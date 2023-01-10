package com.app.surnft.backend.nft.dto

import com.app.surnft.backend.constraint.NullableAddress

class GetAllNftRequest(
  @NullableAddress
  val address: String?,
)
