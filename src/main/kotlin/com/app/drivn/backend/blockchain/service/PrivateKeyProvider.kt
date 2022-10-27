package com.app.drivn.backend.blockchain.service

import com.app.drivn.backend.config.properties.AppProperties
import org.springframework.stereotype.Service
import org.web3j.utils.Numeric

@Service
class PrivateKeyProvider(
  private val appProperties: AppProperties,
) {

  fun fetchPrivateKey(): String =
    decrypt(
      data = Numeric.hexStringToByteArray(appProperties.key),
      password = appProperties.sigKey,
    ).toString(Charsets.UTF_8)
}
