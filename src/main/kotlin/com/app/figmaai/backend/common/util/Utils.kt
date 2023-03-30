package com.app.figmaai.backend.common.util

import org.springframework.data.domain.Persistable
import java.io.PrintWriter
import java.io.StringWriter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

object Utils {
  const val EMPTY = ""

  fun randomString(
    length: Int = 8,
    prefix: String = "",
    lower: Boolean = true,
    upper: Boolean = true,
    digits: Boolean = true
  ): String {
    when {
      length == 0 -> return ""
      length < 0 -> throw Exception(
        "length must be positive"
      )
      !lower && !upper && !digits -> throw Exception(
        "at least one argument must be 'true'"
      )
    }

    val pool: MutableList<Char> = mutableListOf()

    if (lower) pool += ('a'..'z')
    if (upper) pool += ('A'..'Z')
    if (digits) pool += ('0'..'9')

    pool.shuffle()

    return prefix + (1..length)
      .map { pool.random() }
      .joinToString("")
  }

  fun randomStringID(length: Int = 8): String = randomString(length = length, lower = false)

  fun roundUpToHundredths(decimal: BigDecimal): BigDecimal =
    decimal.setScale(2, RoundingMode.UP)

  fun sumRoundUpToHundredths(decimals: Collection<BigDecimal>): BigDecimal =
    decimals
      .fold(BigDecimal.ZERO, BigDecimal::add)
      .let(::roundUpToHundredths)

  fun getStackTrace(throwable: Throwable): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw, true)
    throwable.printStackTrace(pw)
    return sw.buffer.toString()
  }

  private const val errorLogFormat: String = "\n" + "CODE: %s" + "\n" + "MESSAGE: %s" + "\n" + "STACK TRACE: %s"
  private const val exceptionErrorLogFormat: String = "\n" + "CODE: %s" + "\n" + "STACK TRACE: %s"
  private const val errorLogFormatWithoutStackTrace: String = "CODE: %s \t MESSAGE: %s"

  fun createLogErrorMessage(ex: Throwable, code: String): String =
    String.format(exceptionErrorLogFormat, code, getStackTrace(ex))

  fun removeEnd(str: String, remove: String): String {
    if (str.endsWith(remove)) {
      return str.substring(0, str.length - remove.length)
    }
    return str
  }

  fun truncate(text: String, minLength: Int = 50, abbrevMarker: String = ".", maxWith: Int = 2) =
    if (text.length > minLength) text.substring(0, maxWith - abbrevMarker.length) + abbrevMarker
    else text


  const val EXTENSION_SEPARATOR = "."
  const val UNIX_SEPARATOR = '/'
  const val WINDOWS_SEPARATOR = '\\'
  const val FILE_NAME_FORMAT = "%s.%s"

  fun getExtension(filename: String?): String =
    filename
      ?.takeIf { it.isNotBlank() }
      ?.let {
        val extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR)
        val lastSeparator = max(filename.lastIndexOf(UNIX_SEPARATOR), filename.lastIndexOf(WINDOWS_SEPARATOR))
        if (extensionPos == -1 || lastSeparator > extensionPos) EMPTY else filename.substring(extensionPos + 1)
      } ?: EMPTY

  fun getBaseName(filename: String?): String =
    filename
      ?.takeIf { it.isNotBlank() }
      ?.let {
        val lastSeparator = max(filename.lastIndexOf(UNIX_SEPARATOR), filename.lastIndexOf(WINDOWS_SEPARATOR))
        val res = filename.substring(lastSeparator + 1)
        val extensionPos = res.lastIndexOf(EXTENSION_SEPARATOR)
        if (extensionPos == -1) res else res.substring(0, extensionPos)
      } ?: EMPTY

  fun getNameNoExtension(filename: String?): String =
    filename
      ?.takeIf { it.isNotBlank() }
      ?.let {
        val extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR)
        if (extensionPos == -1) it else filename.substring(0, extensionPos)
      } ?: EMPTY

  fun getNameWithExtension(filename: String?, extension: String?): String {
    if (filename.isNullOrBlank())
      return EMPTY
    if (extension.isNullOrBlank()) {
      return filename
    }
    return String.format(FILE_NAME_FORMAT, getNameNoExtension(filename), extension)
  }

  fun getPath(filename: String?): String =
    filename
      ?.takeIf { it.isNotBlank() }
      ?.let { getNameNoExtension(it).substringBeforeLast("/" + getBaseName(it)) }
      ?: EMPTY
}