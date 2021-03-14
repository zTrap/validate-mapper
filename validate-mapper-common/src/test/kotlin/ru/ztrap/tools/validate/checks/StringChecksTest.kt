package ru.ztrap.tools.validate.checks

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.annotations.CheckParametrized
import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.mapper.ValidateMapper

/**
 * @author Peter Gulko (zTrap)
 * @date 11.03.2021
 */
class StringChecksTest {

    @Test
    fun `test not empty string check`() {
        val raw1 = NotEmptyStringCheckTestCase.Raw(string = "")
        val result1 = NotEmptyStringCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(string=)
                |	Params -> string - Reasons -> [{reason="empty string"}]
                """.trimMargin()
            )

        val raw2 = NotEmptyStringCheckTestCase.Raw(string = "123")
        val result2 = NotEmptyStringCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.getOrNull()).isNotNull.hasToString("Mapped(string=123)")
    }

    @Test
    fun `test not blank string check`() {
        val raw1 = NotBlankStringCheckTestCase.Raw(string = "")
        val result1 = NotBlankStringCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(string=)
                |	Params -> string - Reasons -> [{reason="empty string"}]
                """.trimMargin()
            )

        val raw2 = NotBlankStringCheckTestCase.Raw(string = " ")
        val result2 = NotBlankStringCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(string= )
                |	Params -> string - Reasons -> [{reason="blank string"}]
                """.trimMargin()
            )

        val raw3 = NotBlankStringCheckTestCase.Raw(string = "123")
        val result3 = NotBlankStringCheckTestCase.Raw.runCatching { invoke(raw3) }

        assertThat(result3.getOrNull()).isNotNull.hasToString("Mapped(string=123)")
    }

    @Test
    fun `test trimmed string length check`() {
        val raw = TrimmedStringLengthCheckTestCase.Raw(
            success = "123456",
            notInLimits = "123456789",
            minGreaterThanMax = "",
            negativeMin = "",
            negativeMax = ""
        )
        val result = TrimmedStringLengthCheckTestCase.Raw.runCatching { invoke(raw) }

        assertThat(result.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(success=123456, notInLimits=123456789, minGreaterThanMax=, negativeMin=, negativeMax=)
                |	Params -> minGreaterThanMax - Reasons -> [{reason="min limit is greater than max limit", values=[min_limit=8, max_limit=5]}],
                |	          negativeMax ------- Reasons -> [{reason="max limit is negative", values=[max_limit=-1]}],
                |	          negativeMin ------- Reasons -> [{reason="min limit is negative", values=[min_limit=-1]}],
                |	          notInLimits ------- Reasons -> [{reason="length not in limits", values=[min_limit=5, max_limit=8, current_length=9]}]
                """.trimMargin()
            )
    }
}

private object NotEmptyStringCheckTestCase {

    data class Raw(
        @Checks(NotEmptyStringCheck::class)
        val string: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(requireNotNull(raw.string))
        }
    }

    data class Mapped(val string: String)
}

private object NotBlankStringCheckTestCase {

    data class Raw(
        @Checks(NotBlankStringCheck::class)
        val string: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(requireNotNull(raw.string))
        }
    }

    data class Mapped(val string: String)
}

private object TrimmedStringLengthCheckTestCase {
    private const val NEGATIVE_LIMIT = -1L
    private const val MAX_LIMIT = 8L
    private const val MIN_LIMIT = 5L

    data class Raw(
        @CheckParametrized(
            expressionClass = TrimmedStringLengthCheck::class,
            long = [
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, MIN_LIMIT),
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, MAX_LIMIT)
            ]
        )
        val success: String?,
        @CheckParametrized(
            expressionClass = TrimmedStringLengthCheck::class,
            long = [
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, MIN_LIMIT),
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, MAX_LIMIT)
            ]
        )
        val notInLimits: String?,
        @CheckParametrized(
            expressionClass = TrimmedStringLengthCheck::class,
            long = [
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, MAX_LIMIT),
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, MIN_LIMIT)
            ]
        )
        val minGreaterThanMax: String?,
        @CheckParametrized(
            expressionClass = TrimmedStringLengthCheck::class,
            long = [
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MIN_LIMIT_LONG, NEGATIVE_LIMIT)
            ]
        )
        val negativeMin: String?,
        @CheckParametrized(
            expressionClass = TrimmedStringLengthCheck::class,
            long = [
                CheckParametrized.LongParameter(TrimmedStringLengthCheck.MAX_LIMIT_LONG, NEGATIVE_LIMIT)
            ]
        )
        val negativeMax: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(
                success = requireNotNull(raw.success),
                notInLimits = requireNotNull(raw.notInLimits),
                minGreaterThanMax = requireNotNull(raw.minGreaterThanMax),
                negativeMin = requireNotNull(raw.negativeMin),
                negativeMax = requireNotNull(raw.negativeMax)
            )
        }
    }

    data class Mapped(
        val success: String,
        val notInLimits: String,
        val minGreaterThanMax: String,
        val negativeMin: String,
        val negativeMax: String
    )
}