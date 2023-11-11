package com.app.figmaai.backend.ai

data class PexelsResponse(
  val videos: List<Video>,
) {
  data class Video(
    val image: String,
    val video_files: List<VideoFile>,
  ) {
    data class VideoFile(
      val link: String,
      val quality: String,
    )
  }
}