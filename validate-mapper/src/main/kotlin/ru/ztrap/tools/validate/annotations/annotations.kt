package ru.ztrap.tools.validate.annotations

import ru.ztrap.tools.validate.checks.ValidateChecker
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeChecks(vararg val expressionClasses: KClass<out ValidateChecker>)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Checks(vararg val expressionClasses: KClass<out ValidateChecker>)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotRequired