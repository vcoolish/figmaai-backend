package com.app.figmaai.backend.image.service

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import org.springframework.stereotype.Service
import java.util.*


@Service
class AwsS3Service(
  private val amazonS3: AmazonS3
) {

  fun generatePreSignedUrl(
    filePath: String?,
    bucketName: String?,
    httpMethod: HttpMethod?
  ): String {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    calendar.add(Calendar.MINUTE, 10) //validity of 10 minutes
    return amazonS3.generatePresignedUrl(bucketName, filePath, calendar.time, httpMethod)
      .toString()
  }
}