package com.app.drivn.backend.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UnusedReceiverParameter")
inline fun <reified T> T.logger(): Logger {
  return LoggerFactory.getLogger(T::class.java)
}
