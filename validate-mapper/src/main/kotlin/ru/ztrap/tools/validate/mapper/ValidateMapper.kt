package ru.ztrap.tools.validate.mapper

import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.annotations.ExcludeChecks
import ru.ztrap.tools.validate.annotations.NotRequired
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
            throw FailedValidationException(failedParams, raw)
        }

        return transform(raw)
    }

    protected abstract fun transform(raw: T): R

    private fun validate(raw: T) {
        raw::class.java.declaredFields.forEach { property ->
            val skip = property.haveAnnotation<NotRequired>()

            val propertyName = nameExtractor?.invoke(property) ?: property.name

            if (skip) return@forEach

            val value = property.let {
                it.isAccessible = true
                it.get(raw)
            }

            val reasons = arrayListOf<String>()

            if (value != null) {
                val additionChecks = globalChecks.asSequence()
                    .filter { it.key.java.isInstance(value) }
                    .flatMap { it.value.asSequence() }

                val excludedChecks = property.findAnnotation<ExcludeChecks>()
                    ?.expressionClasses
                    ?.asSequence()
                    .orEmpty()

                property.findAnnotation<Checks>()
                    ?.expressionClasses
                    ?.asSequence()
                    .orEmpty()
                    .plus(additionChecks)
                    .minus(excludedChecks)
                    .map { it.getOrCreateInstance() }
                    .map { it(value) }
                    .mapNotNull { it as? ValidateChecker.Result.Error }
                    .map { it.reason }
                    .toCollection(reasons)
            } else {
                reasons.add("null")
            }

            if (reasons.isNotEmpty()) {
                failedParams[propertyName] = reasons
            }
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