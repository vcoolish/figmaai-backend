package com.app.figmaai.backend.chatgpt

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.user.repository.UserRepository
import com.app.figmaai.backend.user.service.TokenProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

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
  ): List<String> {
    val instruction = when (mode) {
      CopyrightMode.translate -> {
        require(!language.isNullOrEmpty()) { "Language must be specified for translate mode" }
        String.format(mode.request, language)
      }
      else -> mode.request
    } + " ${tone?.value ?: ""}"
    val copies = mode.copies
//    return requestEdit(text, instruction.trim(), copies)
    return requestChat("userUuid", text, instruction.trim(), copies)
  }

  fun uxBuilder(
    text: String,
    mode: UxMode,
    token: String,
  ): List<String> {
//    val userUuid = tokenProvider.createParser().parseClaimsJws(token).body.subject
    return requestChat("userUuid", text, mode.value)
  }

  private fun requestChat(userUuid: String, prompt: String, instruction: String, copies: Int = 1): List<String> {
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
      user = userUuid,
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
    return response.body?.choices?.map { it.message?.content ?: "" }
      ?: throw BadRequestException("Failed to create edit")
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
