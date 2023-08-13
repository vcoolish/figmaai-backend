package com.app.figmaai.backend.chatgpt

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.exception.InsufficientBalanceException
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.repository.UserRepository
import com.app.figmaai.backend.user.service.TokenProvider
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import kotlin.math.max

@Service
class ChatGptService(
  private val appProperties: AppProperties,
  private val tokenProvider: TokenProvider,
  private val userRepository: UserRepository,
) {

  val restTemplate = RestTemplate(
    HttpComponentsClientHttpRequestFactory().apply {
      setReadTimeout(60000)
      setConnectTimeout(30000)
    }
  )

  fun copyright(
    text: String,
    mode: CopyrightMode,
    language: String?,
    tone: ChatGptTone?,
    token: String,
  ): List<String> {
    val request = when (mode) {
      CopyrightMode.translate -> {
        require(!language.isNullOrEmpty()) { "Language must be specified for translate mode" }
        String.format(mode.request, language, tone?.value ?: "", text)
      }
      else -> String.format(mode.request, tone?.value ?: "", text)
    }
    val copies = mode.copies
    val userUuid = tokenProvider.createParser().parseClaimsJws(token).body.subject
    val user = userRepository.findByUserUuid(userUuid)

    if (user.credits < 100) {
      throw InsufficientBalanceException("Not enough credits, please renew your subscription")
    }
    if (!user.isSubscribed) {
      throw ExpiredAuthorizationException("Subscription expired")
    }
    val response = requestChat(
      user = user,
      prompt = request.trim(),
      instruction = mode.system,
      copies = copies,
    )
    user.credits -= response?.usage?.total_tokens ?: 0
    user.credits = max(user.credits, 0)
    userRepository.save(user)
    return when (mode) {
      CopyrightMode.paraphrase,
      CopyrightMode.enlonger,
      CopyrightMode.enshorter,
      -> {
        val content = response?.choices?.firstOrNull()?.message?.content
          ?: throw BadRequestException("Failed to create edit")
        Gson().fromJson(content, Array<String>::class.java).toList()
      }

      else ->
        response?.choices?.map { it.message?.content ?: "" }
          ?: throw BadRequestException("Failed to create edit")
    }
  }

  fun uxBuilder(
    text: String,
    mode: UxMode,
    token: String,
  ): List<String> {
    val userUuid = tokenProvider.createParser().parseClaimsJws(token).body.subject
    val user = userRepository.findByUserUuid(userUuid)
    if (user.uxCredits < 100) {
      throw InsufficientBalanceException("Not enough credits, please renew your subscription")
    }
    if (!user.isSubscribed) {
      throw ExpiredAuthorizationException("Subscription expired")
    }

    val response = requestChat(user, text, mode.value)
    user.uxCredits -= response?.usage?.total_tokens ?: 0
    user.credits = max(user.credits, 0)
    userRepository.save(user)
    val choices = response?.choices?.map { it.message?.content ?: "" }
      ?: throw BadRequestException("Failed to create edit")
    return if (mode == UxMode.userpersona) {
      listOf(addPersonaImage(choices.first()))
    } else {
      choices
    }
  }

  private fun addPersonaImage(choice: String): String {
    val map = JsonParser().parse(choice).asJsonObject
    val info = map.getAsJsonObject("basic_info")
    val gender = info.get("gender").asString.lowercase()
    val age = info.get("age").asString.toInt()
    val assets = if (gender == "male") menPics else womenPics
    map.addProperty("image", assets.entries.find { age in it.key }?.value?.random() ?: "")
    return Gson().toJson(map)
  }

  private fun requestChat(
    user: User,
    prompt: String,
    instruction: String,
    copies: Int = 1
  ): EditResponse? {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")

    val body = ChatGptRequest(
      model = ChatModel.CHAT_3_5.value,
      messages = listOf(
        ChatGptRequest.CharMessage(
          role = ChatRole.system.name,
          content = instruction,
        ),
        ChatGptRequest.CharMessage(
          role = ChatRole.user.name,
          content = prompt,
        ),
      ),
      user = user.userUuid,
      n = copies,
    )
    headers.add("Content-Type", "application/json")
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)

    val response = restTemplate.exchange(
      "https://api.openai.com/v1/chat/completions",
      HttpMethod.POST,
      httpEntity,
      EditResponse::class.java
    )
    return response.body
  }

  private fun requestEdit(prompt: String, instruction: String, copies: Int): List<String> {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")

    val body = EditRequest(
      model = ChatModel.DAVINCI.value,
      instruction = instruction,
      input = prompt,
      n = copies,
    )
    headers.add("Content-Type", "application/json")
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)

    val response = restTemplate.exchange(
      "https://api.openai.com/v1/edits",
      HttpMethod.POST,
      httpEntity,
      EditResponse::class.java
    )
    return response.body?.choices?.map { it.text ?: "" }
      ?: throw BadRequestException("Failed to create edit")
  }
}
