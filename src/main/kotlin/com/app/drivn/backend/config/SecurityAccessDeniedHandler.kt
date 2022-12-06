package com.app.drivn.backend.config

import com.app.drivn.backend.common.util.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class SecurityAccessDeniedHandler : AccessDeniedHandler {

  private val logger = logger()

  @Throws(IOException::class)
  override fun handle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    exception: AccessDeniedException
  ) {
    val message = exception.localizedMessage
    logger.debug("Access denied for [${request.method}] ${request.queryString} with $message.")

    response.status = HttpStatus.FORBIDDEN.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.writer.write(message)
  }
}
