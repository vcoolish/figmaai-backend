package com.app.figmaai.backend.image.dto

import com.app.figmaai.backend.constraint.NullableFigma

class GetAllNftRequest(
  @NullableFigma
  val figma: String?,
  val query: String,
)