package ru.ztrap.tools.validate.checks

object NotEmptyIterableCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return when {
            raw !is Iterable<*> -> Result.Error("not an iterable", mapOf(CLASS_NAME to raw.className()))
            !raw.iterator().hasNext() -> Result.Error("empty iterable")
            else -> Result.Success
        }
    }
}