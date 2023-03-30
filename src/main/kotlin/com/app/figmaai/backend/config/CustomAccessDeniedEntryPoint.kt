package com.app.figmaai.backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.Serializable
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomAccessDeniedEntryPoint : AccessDeniedHandler, Serializable {

  @Autowired
  private lateinit var errorServletResponseCreator: ErrorServletResponseCreator

  override fun handle(request: HttpServletRequest, response: HttpServletResponse, exception: AccessDeniedException) {
    exception
      .run { errorServletResponseCreator.build(response, this, HttpStatus.FORBIDDEN) }
  }
}
