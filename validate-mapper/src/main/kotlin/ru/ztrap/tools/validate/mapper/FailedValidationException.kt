package ru.ztrap.tools.validate.mapper

import ru.ztrap.tools.validate.checks.ValidateChecker

private val LINE_SPACING_START = " ".repeat(10)
private val LINE_SEPARATOR = ",\n\t$LINE_SPACING_START"

class FailedValidationException internal constructor(
    val failedParams: Map<String, List<ValidateChecker.Result.Error>>,
    val rawObject: Any,
) : RuntimeException() {
    override val message: String by lazy {
        val sortedParams = failedParams.entries.sortedBy { it.key }
        val maxParamNameLength = sortedParams.maxOf { it.key.length }
        val paramsString = sortedParams.joinToString(LINE_SEPARATOR) {
            "${adaptParamName(it.key, maxParamNameLength)}- Reasons -> ${it.value}"
        }

        "Failed validation of received object.\n" +
            "\tObject -> $rawObject\n" +
            "\tParams -> $paramsString"
    }

    private fun adaptParamName(paramName: String, maxLength: Int): String {
        return if (paramName.length == maxLength) {
            "$paramName "
        } else {
            "$paramName ${"-".repeat(maxLength - paramName.length)}"
        }
    }
}