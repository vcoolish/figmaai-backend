package com.app.figmaai.backend.chatgpt.wallet

import com.app.figmaai.backend.chatgpt.ChatGptRequest
import com.app.figmaai.backend.chatgpt.ChatModel
import com.app.figmaai.backend.chatgpt.ChatRole
import com.app.figmaai.backend.chatgpt.EditResponse
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class ChatGptWalletService(
  private val appProperties: AppProperties,
) {

  val restTemplate = RestTemplate(
    HttpComponentsClientHttpRequestFactory().apply {
      setReadTimeout(60000)
      setConnectTimeout(30000)
    }
  )

  fun play(
    audio: ByteArray,
    mode: PlayMode,
  ): String {
    val response = requestChat(transcribe(audio), mode.value)
    val choices = response?.choices?.map { it.message?.content ?: "" }
      ?: throw BadRequestException("Failed to create edit")
    return choices.first()
  }

  private fun transcribe(audio: ByteArray): String {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")

    val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
    val contentDisposition = ContentDisposition
      .builder("form-data")
      .name("file")
      .filename("audio.webm")
      .build()

    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
    val fileEntity = HttpEntity(audio, fileMap)

    val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
    body.add("file", fileEntity)
    body.add("model", "whisper-1")

    headers.add("Content-Type", "multipart/form-data")
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(
      body,
      headers,
    )

    val response = restTemplate.exchange(
      "https://api.openai.com/v1/audio/transcriptions",
      HttpMethod.POST,
      httpEntity,
      TranscriptionResponse::class.java
    )
    return response.body?.text ?: throw BadRequestException("Failed to transcribe audio")
  }

  private fun requestChat(
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
      user = "",
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
}
