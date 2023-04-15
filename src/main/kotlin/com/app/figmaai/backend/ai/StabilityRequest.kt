package com.app.figmaai.backend.ai

data class StabilityRequest(val text_prompts: List<StabilityPrompt>)
data class StabilityImageRequest(val text_prompts: List<StabilityPrompt>, val init_image: ByteArray)
data class StabilityPrompt(val text: String)