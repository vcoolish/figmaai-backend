package com.app.figmaai.backend.credentials

data class GoogleCredentials (
    override val clientId: String,
    override val clientSecret: String,
    override val scope: String
): SocialCredentials
