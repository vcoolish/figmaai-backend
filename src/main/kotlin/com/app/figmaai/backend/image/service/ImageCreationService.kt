package com.app.figmaai.backend.image.service

import com.app.figmaai.backend.image.mapper.ImageMapper
import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.user.model.User
import org.springframework.stereotype.Service

@Service
class ImageCreationService {

  fun create(user: User): ImageAI {
    val car = ImageMapper.generateCar()

    car.user = user
    car.image = ""

    return car
  }

}
