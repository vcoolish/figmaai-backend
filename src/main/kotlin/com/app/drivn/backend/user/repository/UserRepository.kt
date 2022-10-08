package com.app.drivn.backend.user.repository

import com.app.drivn.backend.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

interface UserRepository : JpaRepository<User, String> {

  @Query(
    "select u.next_energy_renew from users u order by u.next_energy_renew nulls last limit 1",
    nativeQuery = true
  )
  fun getNextRenewTime(): Optional<Instant>

  @Query("select distinct u " +
      "from User u " +
      "where u.nextEnergyRenew <= :nextEnergyRenew or u.energy < u.maxEnergy " +
      "order by u.nextEnergyRenew")
  fun findByNextEnergyRenewLessThanEqualOrderByNextEnergyRenewAsc(
    @Param("nextEnergyRenew") nextEnergyRenew: ZonedDateTime
  ): Set<User>

}
