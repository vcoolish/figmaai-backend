package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.user.model.User
import io.jsonwebtoken.*
import liquibase.repackaged.org.apache.commons.lang3.time.DateUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream
import kotlin.reflect.KClass
import org.springframework.security.core.userdetails.User as SpringSecurityUser

@Component
class TokenProvider(
  private val keyStore: JwtKeyStore,
) {
  private val accessTokenExpirationTime: Long = 1 * DateUtils.MILLIS_PER_MINUTE
  private val refreshTokenExpirationTime: Long = 5 * DateUtils.MILLIS_PER_MINUTE

  companion object {
    private const val SUBJECT_API = "system-api"
    const val AUTHORITIES_KEY = "auth"
    const val PERMISSIONS_KEY = "perms"
    private const val VALIDILITY_VALUE = 1000L
    private const val EMPTY = ""
    private val log = logger()
    private val jwtExceptions = JWTLogException.values()
  }

  fun getClaimsFromToken(token: String?): Claims =
    createParser()
      .parseClaimsJws(token)
      .body

  fun createAccessToken(user: User): String {
    val validity = Date(System.currentTimeMillis() + accessTokenExpirationTime)

    return Jwts.builder()
      .setSubject(user.userUuid)
      .signWith(SignatureAlgorithm.RS256, keyStore.private)
      .setExpiration(validity)
      .compact()
  }

  fun createRefreshToken(subject: String?): String {
    val validity = Date(System.currentTimeMillis() + refreshTokenExpirationTime)

    return Jwts.builder()
      .setSubject(subject)
      .signWith(SignatureAlgorithm.RS256, keyStore.private)
      .setExpiration(validity)
      .compact()
  }

  fun createParser(): JwtParser = Jwts.parser().setSigningKey(keyStore.public)

  fun isClaimsContainsAuthKey(claims: Claims): Boolean =
    claims.containsKey(AUTHORITIES_KEY)

  fun getAuthentication(claims: Claims, credentials: Any? = null): Authentication {

    val principal = SpringSecurityUser(claims.subject, EMPTY, listOf(GrantedAuthority { "" }))
    return UsernamePasswordAuthenticationToken(principal, credentials, listOf(GrantedAuthority { "" }))
  }

  private fun compress(text: String): ByteArray = compress(text.toByteArray())

  private fun compress(bArray: ByteArray): ByteArray {
    val os = ByteArrayOutputStream()
    DeflaterOutputStream(os).use { dos -> dos.write(bArray) }
    return os.toByteArray()
  }

  private fun decompress(text: String): ByteArray = decompress(Base64.getDecoder().decode(text))

  private fun decompress(compressedTxt: ByteArray): ByteArray {
    val os = ByteArrayOutputStream()
    InflaterOutputStream(os).use { ios -> ios.write(compressedTxt) }
    return os.toByteArray()
  }

  enum class JWTLogException(val clazz: KClass<out Exception>) {
    SECURITY_EXCEPTION(SecurityException::class) {
      override fun printLogs(e: Exception) {
        log.info("Invalid JWT signature.")
        log.trace("Invalid JWT signature trace: {}", e)
      }
    },
    MALFORMED_JWT_EXCEPTION(MalformedJwtException::class) {
      override fun printLogs(e: Exception) {
        log.info("Invalid JWT signature.")
        log.trace("Invalid JWT signature trace: {}", e)
      }
    },
    EXPIRED_JWT_EXCEPTION(ExpiredJwtException::class) {
      override fun printLogs(e: Exception) {
        log.info("Expired JWT token.")
        log.trace("Expired JWT token trace: {}", e)
      }
    },
    UNSUPPORTED_JWT_EXCEPTION(UnsupportedJwtException::class) {
      override fun printLogs(e: Exception) {
        log.info("Unsupported JWT token.")
        log.trace("Unsupported JWT token trace: {}", e)
      }
    },
    ILLEGAL_ARGUMENT_EXCEPTION(IllegalArgumentException::class) {
      override fun printLogs(e: Exception) {
        log.info("JWT token compact of handler are invalid.")
        log.trace("JWT token compact of handler are invalid trace: {}", e)
      }
    },
    JWT_EXCEPTION(JwtException::class) {
      override fun printLogs(e: Exception) {
        log.info("An JwtException parsing JWT token.")
        log.trace("An JwtException parsing JWT token.: {}", e)
      }
    };

    abstract fun printLogs(e: Exception)
  }

  fun logError(e: Exception) {
    jwtExceptions
      .firstOrNull { it.clazz == e::class }
      ?.run { printLogs(e) }
  }
}