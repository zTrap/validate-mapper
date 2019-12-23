package ru.ztrap.tools.validate.checks

abstract class ValidateChecker {
    abstract operator fun invoke(raw: Any, parameters: Map<String, Any>): Result

    sealed class Result(val reason: String) {
        object Success : Result("")
        class Error(reason: String) : Result(reason)
    }
}