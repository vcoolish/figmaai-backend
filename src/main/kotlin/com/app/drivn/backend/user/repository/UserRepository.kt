package com.app.drivn.backend.user.repository

import com.app.drivn.backend.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String>
