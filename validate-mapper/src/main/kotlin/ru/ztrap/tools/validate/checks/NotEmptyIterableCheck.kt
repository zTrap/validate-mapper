package ru.ztrap.tools.validate.checks

import ru.ztrap.tools.validate.checks.ValidateChecker.Result

object NotEmptyIterableCheck : ValidateChecker {

    override fun invoke(raw: Any): Result {
        return if (raw is Iterable<*>) {
            if (raw.iterator().hasNext()) {
                Result.Success
            } else {
                Result.Error("empty iterable")
            }
        } else {
            Result.Error("not an iterable")
        }
    }
}