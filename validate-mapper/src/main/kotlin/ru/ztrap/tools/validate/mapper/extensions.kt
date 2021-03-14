package ru.ztrap.tools.validate.mapper

import java.lang.reflect.Field

fun <T : Any> Iterable<T?>?.deepAssert(): List<T> = requireNotNull(this).map { requireNotNull(it) }
fun <T : Any, R : Any> T?.validateMap(mapper: ValidateMapper<T, R>): R = requireNotNull(this).let(mapper)
fun <T : Any, R : Any> Iterable<T?>?.validateMap(mapper: ValidateMapper<T, R>): List<R> = deepAssert().map(mapper)

inline fun <reified A : Annotation> Field.findAnnotation(): A? = getAnnotation(A::class.java)
inline fun <reified A : Annotation> Field.haveAnnotation(): Boolean = findAnnotation<A>() != null
inline fun <reified A : Annotation> Field.notHaveAnnotation(): Boolean = !haveAnnotation<A>()