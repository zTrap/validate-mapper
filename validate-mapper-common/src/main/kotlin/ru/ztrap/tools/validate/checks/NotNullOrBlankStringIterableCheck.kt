package ru.ztrap.tools.validate.checks

object NotNullOrBlankStringIterableCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return if (raw is Iterable<*>) {
            if (raw.iterator().hasNext()) {
                if (raw.iterator().next()?.javaClass == String::class.java) {
                    if (raw.none { (it as String?).isNullOrBlank() }) {
                        Result.Success
                    } else {
                        Result.Error("iterable with only nulls or blank strings")
                    }
                } else {
                    Result.Error("iterable with not a string as component type")
                }
            } else {
                Result.Error("empty iterable")
            }
        } else {
            Result.Error("not an iterable")
        }
    }
}