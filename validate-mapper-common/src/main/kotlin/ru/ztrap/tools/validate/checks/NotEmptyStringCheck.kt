package ru.ztrap.tools.validate.checks

object NotEmptyStringCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return if (raw is String) {
            if (raw.isNotEmpty()) {
                Result.Success
            } else {
                Result.Error("empty string")
            }
        } else {
            Result.Error("not a string")
        }
    }
}