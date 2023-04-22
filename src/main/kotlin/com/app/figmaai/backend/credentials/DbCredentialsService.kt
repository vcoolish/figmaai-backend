package com.app.figmaai.backend.credentials

interface DbCredentialsService {

    fun getActiveSocialNetworks(): ActiveSocialNetworks
    fun updateActiveSocialNetworks(activeSocialNetworks: ActiveSocialNetworks): Boolean

    fun getGoogleCredentials(): GoogleCredentials
    fun updateGoogleCredentials(credentials: GoogleCredentials): Boolean
}
