package com.app.drivn.backend.blockchain.entity

import java.math.BigDecimal
import java.math.BigInteger

class Unit(
  val decimals: Int,
  val symbol: String,
  val tokenSymbol: String,
) {

  constructor(decimals: Int, symbol: String) : this(
    decimals,
    symbol,
    symbol
  )

  fun toUnit(value: String): BigInteger {
    return toUnit(BigDecimal(value))
  }

  fun toUnit(value: BigDecimal): BigInteger {
    return value.multiply(BigDecimal.TEN.pow(decimals)).toBigInteger()
  }

  fun toValue(value: BigDecimal): BigDecimal {
    return value.divide(BigDecimal.TEN.pow(decimals))
  }

  fun toValue(value: BigInteger): BigDecimal {
    return toValue(BigDecimal(value))
  }

  companion object {

    val DEFAULT = Unit(18, "", "")
  }
}
