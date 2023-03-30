package com.app.figmaai.backend.constraint

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.NotNull
import kotlin.reflect.KClass

@NotNull
@Constraint(validatedBy = [])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Figma(
    val message: String = "invalid figma",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
