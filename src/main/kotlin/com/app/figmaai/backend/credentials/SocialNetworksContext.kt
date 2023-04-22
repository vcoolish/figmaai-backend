package com.app.figmaai.backend.credentials

import com.app.figmaai.backend.user.model.Providers

data class SocialNetworksContext (
    val activeNetworks: List<Providers>
)
