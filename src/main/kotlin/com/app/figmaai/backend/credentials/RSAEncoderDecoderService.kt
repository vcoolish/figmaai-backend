package com.app.figmaai.backend.credentials

import com.app.figmaai.backend.user.service.JwtKeyStore
import java.util.*
import javax.crypto.Cipher

class RSAEncoderDecoderService(
    private val keyStore: JwtKeyStore
) : EncoderDecoderService {

    private val nullChar = '\u0000'

    override fun decode(content: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keyStore.private)
        val cipherContentBytes = Base64.getDecoder().decode(content.toByteArray())
        val decryptedContent = cipher.doFinal(cipherContentBytes)
        return cleanDecryptedString(String(decryptedContent, Charsets.UTF_8))
    }

    private fun cleanDecryptedString(decryptedString: String): String =
        decryptedString.filter { it != nullChar }

    override fun encode(content: String): String {
        val contentBytes = content.trim().toByteArray()
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore.public)
        val cipherContent = cipher.doFinal(contentBytes)
        return Base64.getEncoder().encodeToString(cipherContent)
    }
}
