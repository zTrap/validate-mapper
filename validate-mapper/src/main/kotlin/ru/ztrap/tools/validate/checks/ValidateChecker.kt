package ru.ztrap.tools.validate.checks

abstract class ValidateChecker {

    companion object {
        const val CLASS_NAME = "class_name"
    }

    abstract operator fun invoke(raw: Any, parameters: Map<String, Any>): Result

    protected inline fun <reified T : Any> T.className(): String {
        return this::class.java.canonicalName
    }

    sealed class Result {
        object Success : Result()
        data class Error(val reason: String, val values: Map<String, Any> = emptyMap()) : Result() {

            override fun toString(): String = if (values.isEmpty()) {
                "{reason=\"$reason\"}"
            } else {
                val valuesString = values.entries.joinToString(prefix = "[", postfix = "]") { "${it.key}=${it.value}" }
                "{reason=\"$reason\", values=$valuesString}"
            }
        }
    }
}