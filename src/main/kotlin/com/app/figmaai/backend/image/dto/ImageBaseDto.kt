package com.app.figmaai.backend.image.dto

open class ImageBaseDto {

  lateinit var name: String
  lateinit var description: String
  lateinit var image: String
  var gif: String? = ""
  lateinit var prompt: String
}
