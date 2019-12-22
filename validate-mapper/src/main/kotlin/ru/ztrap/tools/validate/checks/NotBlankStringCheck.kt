package ru.ztrap.tools.validate.checks

import ru.ztrap.tools.validate.checks.ValidateChecker.Result

object NotBlankStringCheck : ValidateChecker {

    override fun invoke(raw: Any): Result {
        return if (raw is String) {
            if (raw.isNotBlank()) {
                Result.Success
            } else {
                Result.Error("blank string")
            }
        } else {
            Result.Error("not a string")
        }
    }
}