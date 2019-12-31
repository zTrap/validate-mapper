package ru.ztrap.tools.validate.checks

object NotBlankStringCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return if (raw is String) {
            when {
                raw.isEmpty() -> Result.Error("empty string")
                raw.isBlank() -> Result.Error("blank string")
                else -> Result.Success
            }
        } else {
            Result.Error("not a string")
        }
    }
}