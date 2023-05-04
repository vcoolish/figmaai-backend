package com.app.figmaai.backend.ai

enum class AiProvider(val energy: Int) {
  DALLE(10),
  MIDJOURNEY(10),
  STABILITY(10),
}

enum class AiVersion {
  V4,
  V5
}