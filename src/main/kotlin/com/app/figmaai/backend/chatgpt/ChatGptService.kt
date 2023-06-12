package com.app.figmaai.backend.chatgpt

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.user.repository.UserRepository
import com.app.figmaai.backend.user.service.TokenProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class ChatGptService(
  private val appProperties: AppProperties,
  private val tokenProvider: TokenProvider,
  private val userRepository: UserRepository,
) {

  val restTemplate = RestTemplate()

  fun copyright(
    text: String,
    mode: CopyrightMode,
    language: String?,
  ): String {
    val instruction = when (mode) {
      CopyrightMode.translate -> {
        require(!language.isNullOrEmpty()) { "Language must be specified for translate mode" }
        String.format(mode.request, language)
      }
      else -> mode.request
    }
    return requestEdit(text, instruction)
  }

  fun uxBuilder(
    text: String,
    mode: UxMode,
  ): String {
    return requestEdit(text, mode.value)
  }

//  private fun requestChat(userUuid: String, prompt: String): ImageAI {
//    val headers = LinkedMultiValueMap<String, String>()
//    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")
//    headers.add("OpenAI-Organization", "org-PPCMBOiIcK9DBzlYoBqyNeFJ")
//
//    val body = ChatGptRequest(
//      model = ChatModel.CHAT_3_5.value,
//      messages = listOf(
//        ChatGptRequest.CharMessage(
//          role = ChatRole.user.name,
//          content = prompt,
//        ),
//      ),
//      user = userUuid,
//    )
//    headers.add("Content-Type", "application/json")
//    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)
//
//    val response = restTemplate.exchange(
//      "https://api.openai.com/v1/chat/completions",
//      HttpMethod.POST,
//      httpEntity,
//      com.app.figmaai.backend.ai.DalleResponse::class.java
//    )
//    val createdPicUrl = response.body?.data?.firstOrNull()?.url
//      ?: throw BadRequestException("Failed to create image")
//  }

  private fun requestEdit(prompt: String, instruction: String): String {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")
//    headers.add("OpenAI-Organization", "org-PPCMBOiIcK9DBzlYoBqyNeFJ")

    val body = EditRequest(
      model = ChatModel.DAVINCI.value,
      instruction = instruction,
      input = prompt,
    )
    headers.add("Content-Type", "application/json")
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)

    val response = restTemplate.exchange(
      "https://api.openai.com/v1/edits",
      HttpMethod.POST,
      httpEntity,
      EditResponse::class.java
    )
    return response.body?.choises?.firstOrNull()?.text
      ?: throw BadRequestException("Failed to create edit")
  }
}
