package com.app.drivn.backend.common.blockchain

import org.bouncycastle.crypto.generators.SCrypt
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than 2^(128 * r / 8).
 */
const val N = 1 shl 9

/**
 * Parallelization parameter. Must be a positive integer less than or equal to Integer.MAX_VALUE / (128 * r * 8).
 */
const val P = 1

@Throws(Exception::class)
fun encrypt(data: ByteArray, password: String): ByteArray {
  var salt = ByteArray(32)
  Arrays.fill(salt, 's'.code.toByte())
  val encryptKey = SCrypt.generate(password.toByteArray(charset("UTF-8")), salt, N, 8, P, 32)
  salt = ByteArray(16)
  Arrays.fill(salt, 'i'.code.toByte())
  val ivKey = SCrypt.generate(password.toByteArray(charset("UTF-8")), salt, N, 8, P, 16)
  val key: SecretKey = SecretKeySpec(encryptKey, "AES")
  val iv = IvParameterSpec(ivKey)
  val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  cipher.init(Cipher.ENCRYPT_MODE, key, iv)
  return cipher.doFinal(data)
}

@Throws(Exception::class)
fun decrypt(data: ByteArray, password: String): ByteArray {
  var salt = ByteArray(32)
  Arrays.fill(salt, 's'.code.toByte())
  val encryptKey = SCrypt.generate(password.toByteArray(charset("UTF-8")), salt, N, 8, P, 32)
  salt = ByteArray(16)
  Arrays.fill(salt, 'i'.code.toByte())
  val ivKey = SCrypt.generate(password.toByteArray(charset("UTF-8")), salt, N, 8, P, 16)
  val key: SecretKey = SecretKeySpec(encryptKey, "AES")
  val iv = IvParameterSpec(ivKey)
  val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  cipher.init(Cipher.DECRYPT_MODE, key, iv)
  return cipher.doFinal(data)
}