package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@Repository
interface UserRepository : JpaSpecificationRepository<User> {

  fun findByFigma(figma: String): List<User>

  @Query(
    "select u.next_energy_renew from users u order by u.next_energy_renew nulls last limit 1",
    nativeQuery = true
  )
  fun getNextRenewTime(): Optional<Instant>

  @Query(
    "select distinct u " +
        "from User u " +
        "where u.nextEnergyRenew <= :nextEnergyRenew or u.energy < u.maxEnergy " +
        "order by u.nextEnergyRenew"
  )
  fun findByNextEnergyRenewLessThanEqualOrderByNextEnergyRenewAsc(
    @Param("nextEnergyRenew") nextEnergyRenew: ZonedDateTime
  ): Set<User>

  fun findOneByEmail(email: String): User?

  @Query(
    """
        SELECT DISTINCT u
        FROM User u
        WHERE lower(u.email) = :email
    """
  )
  fun findByEmail(@Param("email") email: String?): User?

  fun existsByUserUuid(uuid: String): Boolean

  @Query(
    """
        SELECT DISTINCT u
        FROM User u
        WHERE lower(u.email) = :email AND u.verified = true
    """
  )
  fun findVerified(@Param("email") email: String?): User?

  @Query(
    """
        SELECT u FROM User u
        WHERE lower(u.email) = lower(:login)
    """
  )
  fun findUserByLoginWithAllRolesAndBlacklist(@Param("login") login: String?): User?

  @Query(
    """
            SELECT u FROM User u
            WHERE u.userUuid = :uuid
        """
  )
  fun findUserWithAuthorities(@Param("uuid") uuid: String): User?

  @Query(
    """
            select distinct (u) from User u where u.id = :id
        """
  )
  fun fetchByID(@Param("id") id: Long): User?

  @Query(
    """
            SELECT distinct (u) FROM User u
            WHERE u.id = :id
        """
  )
  fun findUserWithAllRoles(@Param("id") id: Long): User?

  fun findOneByGoogleId(googleId: String): User?

  @Query(
    """
            SELECT u.userUuid
            FROM User u
            WHERE u.userUuid in :uuids 
                AND u.verified = true 
                AND u.enabled = true 
                AND u.deleted = false
        """
  )
  fun filterActiveUserUuids(uuids: Collection<String>): Collection<String>

}
