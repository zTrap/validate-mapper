package ru.ztrap.tools.validate.mapper

import ru.ztrap.tools.validate.checks.ValidateChecker

class FailedValidationException(
    val failedParams: Map<String, List<ValidateChecker.Result.Error>>,
    val rawObject: Any
) : RuntimeException(
    "Failed validation of received object.\n" +
            "\tObject -> $rawObject\n" +
            "\tParams -> ${failedParams.entries.joinToString(",\n\t\t\t  ") { "${it.key}; Reasons -> ${it.value}" } }"
)