package ru.ztrap.tools.validate.mapper

import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.annotations.ExcludeChecks
import ru.ztrap.tools.validate.annotations.NotRequired
import ru.ztrap.tools.validate.annotations.Parameters
import ru.ztrap.tools.validate.checks.ValidateChecker
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass

abstract class ValidateMapper<T : Any, R> : (T) -> R {
    private val failedParams = hashMapOf<String, List<String>>()

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
                    val additionChecks = globalChecks.asSequence()
                        .filter { it.key.java.isInstance(value) }
                        .flatMap { it.value.asSequence() }

                    val excludedChecks = field.findAnnotation<ExcludeChecks>()
                        ?.expressionClasses
                        ?.asSequence()
                        .orEmpty()

                    field.findAnnotation<Checks>()
                        ?.expressionClasses
                        ?.asSequence()
                        .orEmpty()
                        .plus(additionChecks)
                        .minus(excludedChecks)
                        .map { it.getOrCreateInstance() to field.parametersFor(it) }
                        .map { (checker, params) -> checker.invoke(value, params) }
                        .map { it.reason }
                        .filter { it.isNotBlank() }
                        .toList()
                }
                required -> listOf("null")
                else -> return@forEach
            }

            if (reasons.isNotEmpty()) {
                failedParams[fieldName] = reasons
            }
        }
    }

    private fun Field.parametersFor(cls: KClass<out ValidateChecker>): Map<String, Any> {
        val annotation = findAnnotation<Parameters>()

        return if (annotation != null && cls == annotation.forChecker) {
            annotation.run {
                emptySequence<Pair<String, Any>>()
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
        } else {
            mapOf()
        }
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
        private val globalChecks = ConcurrentHashMap<KClass<*>, ConcurrentSkipListSet<KClass<out ValidateChecker>>>()
        private var nameExtractor: ((Field) -> String)? = null

        @JvmStatic fun addGlobalCheck(clsCheckPair: Pair<Class<*>, Class<out ValidateChecker>>): Companion {
            return addGlobalCheck(clsCheckPair.first, clsCheckPair.second)
        }

        @JvmStatic fun addGlobalCheck(clsToCheck: Class<*>, clsWhoCheck: Class<out ValidateChecker>): Companion {
            globalChecks.getOrPut(clsToCheck.kotlin) { ConcurrentSkipListSet() }.add(clsWhoCheck.kotlin)
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