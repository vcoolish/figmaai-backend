package com.app.surnft.backend.constraint

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@NotNull
@Pattern(regexp = "^0x[\\da-fA-F]{40}$")
@Constraint(validatedBy = [])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Address(
    val message: String = "invalid address",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
