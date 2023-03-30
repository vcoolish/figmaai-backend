package com.app.figmaai.backend.common.validator

import javax.validation.Constraint
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Constraint(validatedBy = [])
@Target(FUNCTION, FIELD, ANNOTATION_CLASS, PROPERTY_GETTER, VALUE_PARAMETER)
@Retention(RUNTIME)
@Pattern(
    regexp = "^[0-9a-zA-Z]{4,20}\$",
    message = "{nickname.not_valid}"
)
@Size(min = 4, max = 20)
annotation class ValidNickName(
    val message: String = "{nickname.not_valid}",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)
