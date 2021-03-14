package ru.ztrap.tools.validate.checks

object NotEmptyStringCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return when {
            raw !is String -> Result.Error("not a string", mapOf(CLASS_NAME to raw.className()))
            raw.isEmpty() -> Result.Error("empty string")
            else -> Result.Success
        }
    }
}