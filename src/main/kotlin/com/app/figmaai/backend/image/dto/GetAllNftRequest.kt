package com.app.figmaai.backend.image.dto

import com.app.figmaai.backend.constraint.NullableFigma

class GetAllNftRequest(
  @NullableFigma
  val figma: String?,
  val query: String = "",
  val searchType: SearchType = SearchType.all,
)

enum class SearchType {
  all,
  animated,
  static,
  video,
}
