package ru.ztrap.tools.validate.checks

abstract class ValidateChecker {
    abstract operator fun invoke(raw: Any, parameters: Map<String, Any>): Result

    sealed class Result {
        object Success : Result()
        data class Error(val reason: String, val args: Array<Any> = emptyArray()) : Result() {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Error

                if (reason != other.reason) return false
                if (!args.contentEquals(other.args)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = reason.hashCode()
                result = 31 * result + args.contentHashCode()
                return result
            }

            override fun toString(): String {
                return "{reason=\"$reason\", args=${args.contentToString()}}"
            }


        }
    }
}