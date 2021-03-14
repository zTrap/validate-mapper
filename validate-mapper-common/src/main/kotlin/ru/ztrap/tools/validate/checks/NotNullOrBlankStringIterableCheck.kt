package ru.ztrap.tools.validate.checks

object NotNullOrBlankStringIterableCheck : ValidateChecker() {

    override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
        return when {
            raw !is Iterable<*> -> Result.Error("not an iterable", mapOf(CLASS_NAME to raw.className()))
            !raw.iterator().hasNext() -> Result.Error("empty iterable")
            raw.all { it == null } -> Result.Error("iterable with only nulls")
            raw.first { it != null }?.javaClass != String::class.java ->
                Result.Error("iterable with not a string as component type")
            raw.all { (it as String?)?.isBlank() == true } ->
                Result.Error("iterable with only blank strings")
            raw.any { (it as String?).isNullOrBlank() } ->
                Result.Error("iterable with only nulls and blank strings")
            else -> Result.Success
        }
    }
}