package ru.ztrap.tools.validate.checks

import ru.ztrap.tools.validate.checks.ValidateChecker.Result

object NotEmptyStringCheck : ValidateChecker {

    override fun invoke(raw: Any): Result {
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