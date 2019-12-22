package ru.ztrap.tools.validate.checks

interface ValidateChecker {
    operator fun invoke(raw: Any): Result

    sealed class Result {
        object Success : Result()
        class Error(val reason: String) : Result()
    }
}