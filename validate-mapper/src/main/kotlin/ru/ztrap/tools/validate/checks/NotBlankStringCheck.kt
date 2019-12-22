package ru.ztrap.tools.validate.checks

import ru.ztrap.tools.validate.checks.ValidateChecker.Result

object NotBlankStringCheck : ValidateChecker {

    override fun invoke(raw: Any): Result {
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