package com.app.figmaai.backend.user.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@Component
class JwtKeyStore(
  @param:Value("classpath:public_key.der")
  private val publicKey: Resource,
  @param:Value("classpath:private_key.der")
  private val privateKey: Resource
) {

  val public: PublicKey = publicKey.inputStream.use {
    KeyFactory
      .getInstance("RSA")
      .generatePublic(X509EncodedKeySpec(it.readBytes()))
  }

  val private: PrivateKey = privateKey.inputStream.use {
    KeyFactory
      .getInstance("RSA")
      .generatePrivate(PKCS8EncodedKeySpec(it.readBytes()))
  }
}