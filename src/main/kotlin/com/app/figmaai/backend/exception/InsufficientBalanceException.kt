package com.app.figmaai.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InsufficientBalanceException : RuntimeException {

  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)

}
