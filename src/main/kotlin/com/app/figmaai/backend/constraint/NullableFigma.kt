package com.app.figmaai.backend.constraint

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NullableFigma(
    val message: String = "invalid figma",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
