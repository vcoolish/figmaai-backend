package com.app.surnft.backend.nft.model

import com.app.surnft.backend.user.model.User
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "collections")
class Collection {

  @Id
  var id: Long = System.currentTimeMillis() / 1000

  lateinit var address: String

  var count: Int = 0

  lateinit var name: String
  lateinit var symbol: String

  @ManyToOne
  @JoinColumn(name = "user_address")
  lateinit var user: User

  @CreatedDate
  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime
}
