package com.app.drivn.backend.common.blockchain.entity

import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.min

open class Value(val raw: String?) {

  constructor(value: Long?) : this(value?.toString())
  constructor(value: BigInteger?) : this(value?.toString())
  constructor(value: BigDecimal?) : this(value?.toString())

  fun rawString(): String? {
    return raw
  }

  fun bigDecimal(): BigDecimal = try {
    BigDecimal(raw)
  } catch (t: Throwable) {
    BigDecimal.ZERO
  }

  fun bigInteger(): BigInteger {
    return bigDecimal().toBigInteger()
  }

  open fun convert(): BigDecimal {
    return bigDecimal()
  }

  fun shortFormat(none: String?, showSignMode: Int): String {
    return format(2, none, showSignMode)
  }

  fun mediumFormat(none: String?, showSignMode: Int): String {
    return format(6, none, showSignMode)
  }

  fun fullFormat(none: String? = "", showSignMode: Int = MINUS_ONLY): String {
    return format(NONE_ROUND, none, showSignMode)
  }

  open fun format(decimalPlace: Int, empty: String?, showSignMode: Int): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
    formatter.maximumFractionDigits = Int.MAX_VALUE
    val value = convert()
    var result: String
    var resultValue: String

    if (decimalPlace == NONE_ROUND) {
      resultValue = value.abs().toString()
      result = formatter.format(value.abs())
    } else {
      val fullPlainString = value.stripTrailingZeros().abs().toPlainString()
      resultValue = value.toBigInteger().abs().toString()
      result = formatter.format(value.toBigInteger().abs())
      var fractionString = fullPlainString.substring(resultValue.length)
      val minDecimalPlace = min(decimalPlace, fractionString.length - 1)
      if (minDecimalPlace > 0) {
        fractionString = fractionString.substring(0, minDecimalPlace + 1)
        resultValue += fractionString
        result = formatter.format(BigDecimal(resultValue)) //result + fractionString;
      }
    }

    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2

    if (value.compareTo(BigDecimal.ZERO) == 0) {
      result = empty ?: formatter.format(0.00)
    } else if (BigDecimal(resultValue).compareTo(BigDecimal.ZERO) == 0) {
      result = formatter.format(0.00)
    }

    if (result != null && result != empty) {
      if (showSignMode == MINUS_ONLY) {
        result = (if (value < BigDecimal.ZERO) "-" else "") + result
      } else if (showSignMode == ALL_SIGN) {
        result = (if (value >= BigDecimal.ZERO) "+" else "-") + result
      }
    }
    return result
  }

  companion object {

    private const val NONE_ROUND = -1
    const val MINUS_ONLY = -1
    const val NO_SIGN = 0
    const val ALL_SIGN = 1
  }
}