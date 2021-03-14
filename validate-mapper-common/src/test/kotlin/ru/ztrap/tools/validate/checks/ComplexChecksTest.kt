package ru.ztrap.tools.validate.checks

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.annotations.NotRequired
import ru.ztrap.tools.validate.mapper.ValidateMapper
import ru.ztrap.tools.validate.mapper.deepAssert

/**
 * @author Peter Gulko (zTrap)
 * @date 11.03.2021
 */
class ComplexChecksTest {

    @Test
    fun `test all checks`() {
        val raw = TestCase.Raw(
            required = null,
            notRequired = null,
            emptyIterable = emptyList(),
            nullOrBlankStringIterable = listOf(null, " "),
            blankString = " ",
            emptyString = ""
        )
        val result = TestCase.Raw.runCatching { invoke(raw) }

        assertThat(result.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(required=null, notRequired=null, emptyIterable=[], nullOrBlankStringIterable=[null,  ], blankString= , emptyString=)
                |	Params -> blankString --------------- Reasons -> [{reason="blank string"}],
                |	          emptyIterable ------------- Reasons -> [{reason="empty iterable"}],
                |	          emptyString --------------- Reasons -> [{reason="empty string"}],
                |	          nullOrBlankStringIterable - Reasons -> [{reason="iterable with only nulls and blank strings"}],
                |	          required ------------------ Reasons -> [{reason="value is null"}]
                """.trimMargin()
            )
    }
}

private object TestCase {

    data class Raw(
        val required: String?,
        @NotRequired
        val notRequired: String?,
        @Checks(NotEmptyIterableCheck::class)
        val emptyIterable: Iterable<Any>?,
        @Checks(NotNullOrBlankStringIterableCheck::class)
        val nullOrBlankStringIterable: Iterable<String?>?,
        @Checks(NotBlankStringCheck::class)
        val blankString: String?,
        @Checks(NotEmptyStringCheck::class)
        val emptyString: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(
                required = requireNotNull(raw.required),
                notRequired = raw.notRequired,
                emptyIterable = raw.emptyIterable.deepAssert(),
                nullOrBlankStringIterable = raw.nullOrBlankStringIterable.deepAssert(),
                blankString = requireNotNull(raw.blankString),
                emptyString = requireNotNull(raw.emptyString)
            )
        }
    }

    data class Mapped(
        val required: String,
        val notRequired: String?,
        val emptyIterable: Iterable<Any>,
        val nullOrBlankStringIterable: Iterable<String>,
        val blankString: String,
        val emptyString: String
    )
}