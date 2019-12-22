package ru.ztrap.tools.validate.mapper

class FailedValidationException(
    val failedParams: Map<String, List<String>>,
    val rawObject: Any
) : RuntimeException(
    "Failed validation of received object.\n" +
            "\tObject -> $rawObject\n" +
            "\tParams -> ${failedParams.entries.joinToString(",\n\t\t\t  ") { "${it.key}; Reasons -> ${it.value}" } }"
)