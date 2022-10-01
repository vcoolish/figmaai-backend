package com.app.drivn.backend.exception.handler

import com.app.drivn.backend.common.util.LogUtil.*
import com.app.drivn.backend.common.util.logger
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.NoSuchElementException
import javax.persistence.EntityNotFoundException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class GlobalExceptionHandler {

  private fun log(
    ex: Exception,
    request: HttpServletRequest,
    message: String?,
    trace: String
  ) {
    val log = logger()
    log.error(
      """
        Exception: {}
        Message: {}
        httpMethod = {}
        path = {}
        trace = {}
        """.trimIndent(),
      ex, message, request.method, request.servletPath, trace
    )
  }

  private fun log(ex: Exception, request: HttpServletRequest, message: String?) {
    log(ex, request, message, getStacktraceTillSource(ex))
  }


  @ExceptionHandler(
    EntityNotFoundException::class,
    NoHandlerFoundException::class,
    NoSuchElementException::class
  )
  fun handleNotFound(
    exception: Exception,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): ModelAndView {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, exception.message)
    return ModelAndView()
  }

  @ExceptionHandler(Exception::class)
  fun handleException(
    exception: Exception,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): ModelAndView {
    log(exception, request, exception.message)

    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.message)
    return ModelAndView()
  }
}
