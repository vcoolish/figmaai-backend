package com.app.figmaai.backend.ai

data class DalleRequest(val prompt: String)
data class DalleImageRequest(val prompt: String, val image: String)