package com.app.figmaai.backend.credentials

interface SocialCredentials : CredentialsOverloadable {
    val clientId: String
    val clientSecret: String
    val scope: String
}
