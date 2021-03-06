package ru.ztrap.tools.validate.mapper

import java.lang.reflect.Field

fun <T> List<T?>?.deepAssert(): List<T> {
    return this!!.map { it!! }
}

fun <T : Any, R : Any> T?.validateMap(mapper: ValidateMapper<T, R>): R {
    return this!!.let(mapper)
}

fun <T : Any, R : Any> List<T?>?.validateMap(mapper: ValidateMapper<T, R>): List<R> {
    return deepAssert().map(mapper)
}

inline fun <reified A : Annotation> Field.findAnnotation(): A? = getAnnotation(A::class.java)
inline fun <reified A : Annotation> Field.haveAnnotation(): Boolean = findAnnotation<A>() != null
inline fun <reified A : Annotation> Field.notHaveAnnotation(): Boolean = !haveAnnotation<A>()