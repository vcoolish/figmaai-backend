package com.app.drivn.backend.user.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping("/")
    fun index(): String {
        return "Greetings from Drivn!"
    }
}