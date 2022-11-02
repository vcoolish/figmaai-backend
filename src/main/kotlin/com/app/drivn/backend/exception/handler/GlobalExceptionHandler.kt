package com.app.drivn.backend.exception.handler

import com.app.drivn.backend.common.util.LogUtil.*
import com.app.drivn.backend.common.util.logger
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.NoSuchElementException
import javax.persistence.EntityNotFoundException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolationException

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

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(
    exception: HttpMessageNotReadableException,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): ModelAndView {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.message)
    return ModelAndView()
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(
    exception: ConstraintViolationException,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): ModelAndView {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.message)
    return ModelAndView()
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
