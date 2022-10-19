package com.app.drivn.backend.common.blockchain

import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.nio.charset.StandardCharsets
import java.util.*

fun validateMessageSign(
  address: String,
  message: String,
  signature: String,
): Boolean {
  val key = Sign.signedPrefixedMessageToKey(
    message.toByteArray(StandardCharsets.UTF_8),
    getSignatureData(signature),
  )
  val recovered = ("0x" + Keys.getAddress(key)).trim()
  return recovered.equals(address, ignoreCase = true)
}

private fun getSignatureData(signature: String): Sign.SignatureData {
  val signatureBytes = Numeric.hexStringToByteArray(signature)
  var v = signatureBytes[64]
  if (v < 27) {
    v = (v + 27).toByte()
  }
  val r = Arrays.copyOfRange(signatureBytes, 0, 32) as ByteArray
  val s = Arrays.copyOfRange(signatureBytes, 32, 64) as ByteArray
  return Sign.SignatureData(v, r, s)
}