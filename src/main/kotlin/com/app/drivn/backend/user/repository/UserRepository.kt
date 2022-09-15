package com.app.drivn.backend.user.repository

import com.app.drivn.backend.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime
import java.util.*

interface UserRepository : JpaRepository<User, String> {

  @Query("select u.nextEnergyRenew from User u order by u.nextEnergyRenew nulls last ")
  fun getNextRenewTime(): Optional<ZonedDateTime>

  fun findByNextEnergyRenewLessThanEqualOrderByNextEnergyRenewAsc(nextEnergyRenew: ZonedDateTime): List<User>

}
