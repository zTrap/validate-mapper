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

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChecksParametrized(vararg val expressionChecks: CheckParametrized)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
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
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class StringParameter(val name: String, val value: String)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ByteParameter(val name: String, val value: Byte)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ShortParameter(val name: String, val value: Short)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class IntParameter(val name: String, val value: Int)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LongParameter(val name: String, val value: Long)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class FloatParameter(val name: String, val value: Float)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class DoubleParameter(val name: String, val value: Double)

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class KClassParameter(val name: String, val value: KClass<*>)
}