package com.app.figmaai.backend.email

import com.app.figmaai.backend.email.extra.EmailData

interface EmailService {
    fun sendEmail(emailData: EmailData)
}
