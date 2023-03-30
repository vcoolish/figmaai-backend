package com.app.figmaai.backend.ai

import org.springframework.web.multipart.MultipartFile

data class UploadRequest(
  val file: MultipartFile,
)