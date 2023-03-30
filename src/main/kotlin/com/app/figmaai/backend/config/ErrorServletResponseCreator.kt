package com.app.figmaai.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse

@Component
class ErrorServletResponseCreator(private val objectMapper: ObjectMapper) {

  companion object {
    val DEFAULT_ENCODING = StandardCharsets.UTF_8.toString()
  }

  fun build(servletResponse: HttpServletResponse, error: Exception, status: HttpStatus) {
    servletResponse
      .apply {
        this.status = status.value()
        this.contentType = MediaType.APPLICATION_JSON_VALUE
        this.characterEncoding = DEFAULT_ENCODING
      }.also {
        objectMapper.writeValueAsString(error)
          .also { servletResponse.outputStream.use { os -> os.println(it) } }
      }
  }
}