package com.app.figmaai.backend.image.model

import com.app.figmaai.backend.user.model.User
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "images")
class ImageAI : Image() {

  @ManyToOne
  @JoinColumn(name = "user_id")
  lateinit var user: User

  lateinit var prompt: String

  @CreatedDate
  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime

  @LastModifiedDate
  @UpdateTimestamp
  lateinit var updatedAt: ZonedDateTime
}
