package com.app.figmaai.backend.email.impl

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.email.EmailService
import com.app.figmaai.backend.email.extra.EmailData
import com.app.figmaai.backend.email.extra.EmailType
import com.app.figmaai.backend.email.extra.MailSendRequestDto
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EmailServiceImpl(
  private val appProperties: AppProperties,
) : EmailService {

  private val restTemplate = RestTemplate()

  override fun sendEmail(emailData: EmailData) {
    if (emailData.emailType != EmailType.CHANGE_PASSWORD) {
      throw Exception("Email type not supported")
    }
    val link = emailData.dynamicData["link"] as String
    val data = MailSendRequestDto(
      api_key = appProperties.smtpKey,
      sender = "AI Designer Pro <noreply@aidsnpro.com>",
      subject = "Password Recovery",
      text_body = "Here is your link to reset password: $link",
      html_body = "<h1>Reset password link: $link</h1>",
      to = listOf(emailData.userPersonalData.userEmail),
    )
    val requestEntity = HttpEntity<MailSendRequestDto>(data)
    restTemplate.exchange(
      "https://api.smtp2go.com/v3/email/send",
      HttpMethod.POST,
      requestEntity,
      Any::class.java,
    )
  }
}
