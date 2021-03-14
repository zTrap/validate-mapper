package ru.ztrap.tools.validate.checks

object TrimmedStringLengthCheck : ValidateChecker() {
    const val MAX_LIMIT_LONG = "max_limit"
    const val MIN_LIMIT_LONG = "min_limit"
    const val CURRENT_LENGTH = "current_length"

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        val maxLimit = parameters[MAX_LIMIT_LONG]?.toString()?.toLong() ?: Long.MAX_VALUE
        val minLimit = parameters[MIN_LIMIT_LONG]?.toString()?.toLong() ?: 0

        return when {
            raw !is String -> Result.Error("not a string", mapOf(CLASS_NAME to raw.className()))
            maxLimit < 0 -> Result.Error("max limit is negative", mapOf(MAX_LIMIT_LONG to maxLimit))
            minLimit < 0 -> Result.Error("min limit is negative", mapOf(MIN_LIMIT_LONG to minLimit))
            minLimit > maxLimit -> {
                val argsMap = mapOf(
                    MIN_LIMIT_LONG to minLimit,
                    MAX_LIMIT_LONG to maxLimit
                )
                Result.Error("min limit is greater than max limit", argsMap)
            }
            raw.trim().length !in minLimit..maxLimit -> {
                val argsMap = mapOf(
                    MIN_LIMIT_LONG to minLimit,
                    MAX_LIMIT_LONG to maxLimit,
                    CURRENT_LENGTH to raw.trim().length
                )
                Result.Error("length not in limits", argsMap)
            }
            else -> Result.Success
        }
    }
}