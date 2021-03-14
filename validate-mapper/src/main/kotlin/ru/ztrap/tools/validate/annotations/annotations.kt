package ru.ztrap.tools.validate.annotations

import ru.ztrap.tools.validate.checks.ValidateChecker
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
annotation class ExcludeChecks(vararg val expressionClasses: KClass<out ValidateChecker>)

@Target(AnnotationTarget.FIELD)
annotation class Checks(vararg val expressionClasses: KClass<out ValidateChecker>)

@Target(AnnotationTarget.FIELD)
annotation class NotRequired

@Target(AnnotationTarget.FIELD)
annotation class ChecksParametrized(vararg val expressionChecks: CheckParametrized)

@Target(AnnotationTarget.FIELD)
annotation class CheckParametrized(
    val expressionClass: KClass<out ValidateChecker>,
    val string: Array<StringParameter> = [],
    val byte: Array<ByteParameter> = [],
    val short: Array<ShortParameter> = [],
    val int: Array<IntParameter> = [],
    val long: Array<LongParameter> = [],
    val float: Array<FloatParameter> = [],
    val double: Array<DoubleParameter> = [],
    val kclass: Array<KClassParameter> = []
) {
    annotation class StringParameter(val name: String, val value: String)
    annotation class ByteParameter(val name: String, val value: Byte)
    annotation class ShortParameter(val name: String, val value: Short)
    annotation class IntParameter(val name: String, val value: Int)
    annotation class LongParameter(val name: String, val value: Long)
    annotation class FloatParameter(val name: String, val value: Float)
    annotation class DoubleParameter(val name: String, val value: Double)
    annotation class KClassParameter(val name: String, val value: KClass<*>)
}