package ru.ztrap.tools.validate.mapper

import ru.ztrap.tools.validate.annotations.CheckParametrized
import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.annotations.ChecksParametrized
import ru.ztrap.tools.validate.annotations.ExcludeChecks
import ru.ztrap.tools.validate.annotations.NotRequired
import ru.ztrap.tools.validate.checks.ValidateChecker
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass

abstract class ValidateMapper<T : Any, R> : (T) -> R {
    private val failedParams = hashMapOf<String, List<ValidateChecker.Result.Error>>()

    @Throws(FailedValidationException::class)
    override operator fun invoke(raw: T): R {
        failedParams.clear()
        validate(raw)

        if (failedParams.isNotEmpty()) {
            throw FailedValidationException(HashMap(failedParams), raw)
        }

        return transform(raw)
    }

    protected abstract fun transform(raw: T): R

    private fun validate(raw: T) {
        raw::class.java.declaredFields.forEach { field ->
            val required = field.notHaveAnnotation<NotRequired>()

            val fieldName = nameExtractor?.invoke(field) ?: field.name

            val value = field.let {
                it.isAccessible = true
                it.get(raw)
            }

            val reasons = when {
                value != null -> {
                    val globalChecks = globalChecks.asSequence()
                        .filter { it.key.java.isInstance(value) }
                        .flatMap { it.value.asSequence() }

                    val baseChecks = field.findAnnotation<Checks>()
                        ?.expressionClasses
                        ?.asSequence()
                        .orEmpty()
                        .map { it to emptyMap<String, Any>() }

                    val excludedChecks = field.findAnnotation<ExcludeChecks>()
                        ?.expressionClasses
                        ?.asSequence()
                        .orEmpty()
                        .map { it to emptyMap<String, Any>() }

                    val parametrizedChecks = field.findAnnotation<ChecksParametrized>()
                        ?.expressionChecks
                        ?.asSequence()
                        .orEmpty()
                        .let { seq ->
                            field.findAnnotation<CheckParametrized>()
                                ?.let { seq.plus(it) }
                                ?: seq
                        }
                        .map { it.parsed }

                    emptySequence<Pair<CheckerClass, Parameters>>()
                        .plus(baseChecks)
                        .plus(globalChecks)
                        .plus(parametrizedChecks)
                        .minus(excludedChecks)
                        .map { (checkerClass, params) -> checkerClass.getOrCreateInstance() to params }
                        .map { (checker, params) -> checker.invoke(value, params) }
                        .mapNotNull { it as? ValidateChecker.Result.Error }
                        .toList()
                }
                required -> listOf(ValidateChecker.Result.Error("null"))
                else -> return@forEach
            }

            if (reasons.isNotEmpty()) {
                failedParams[fieldName] = reasons
            }
        }
    }

    private val CheckParametrized.parsed: Pair<CheckerClass, Parameters>
        get() {
            return expressionClass to emptySequence<Pair<String, Any>>()
                    .plus(string.map { it.name to (it.value as Any) })
                    .plus(byte.map { it.name to (it.value as Any) })
                    .plus(short.map { it.name to (it.value as Any) })
                    .plus(int.map { it.name to (it.value as Any) })
                    .plus(long.map { it.name to (it.value as Any) })
                    .plus(float.map { it.name to (it.value as Any) })
                    .plus(double.map { it.name to (it.value as Any) })
                    .plus(kclass.map { it.name to (it.value as Any) })
                    .toMap()
        }


    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> KClass<T>.getOrCreateInstance(): T {
        val java = java
        val field = runCatching { java.getField("INSTANCE") }
            .getOrNull()
            ?.takeIf { Modifier.isFinal(it.modifiers) }
            ?.takeIf { Modifier.isStatic(it.modifiers) }

        return field?.get(null) as? T ?: java.newInstance()
    }

    companion object {
        private val globalChecks = ConcMap<KClass<*>, ConcSet<Pair<CheckerClass, Parameters>>>()
        private var nameExtractor: ((Field) -> String)? = null

        @JvmStatic fun addGlobalCheck(clsCheckPair: Pair<Class<*>, Class<out ValidateChecker>>): Companion {
            return addGlobalCheck(clsCheckPair.first, clsCheckPair.second)
        }

        @JvmStatic fun addGlobalCheck(
            clsToCheck: Class<*>,
            clsWhoCheck: Class<out ValidateChecker>,
            parameters: Parameters = emptyMap()
        ): Companion {
            globalChecks.getOrPut(clsToCheck.kotlin) { ConcurrentSkipListSet() }
                .add(clsWhoCheck.kotlin to parameters)
            return this
        }

        @JvmStatic fun setNameExtractor(nameExtractor: (Field) -> String): Companion {
            this.nameExtractor = nameExtractor
            return this
        }

        @JvmStatic fun clearNameExtractor(): Companion {
            this.nameExtractor = null
            return this
        }
    }
}

private typealias CheckerClass = KClass<out ValidateChecker>
private typealias Parameters = Map<String, Any>
private typealias ConcMap<K, V> = ConcurrentHashMap<K, V>
private typealias ConcSet<V> = ConcurrentSkipListSet<V>