package com.app.figmaai.backend.common.validator

import javax.validation.Constraint
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Constraint(validatedBy = [])
@Target(FUNCTION, FIELD, ANNOTATION_CLASS, PROPERTY_GETTER)
@Retention(RUNTIME)
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
    message = "Invalid password"
)
annotation class ValidPassword(
    val message: String = "Invalid password",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)
