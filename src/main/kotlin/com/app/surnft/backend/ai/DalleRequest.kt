package com.app.surnft.backend.ai

data class DalleRequest(val prompt: String)
data class DalleImageRequest(val prompt: String, val image: String)