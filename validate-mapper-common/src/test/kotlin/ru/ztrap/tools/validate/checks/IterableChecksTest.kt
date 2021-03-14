package ru.ztrap.tools.validate.checks

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.annotations.Checks
import ru.ztrap.tools.validate.mapper.ValidateMapper
import ru.ztrap.tools.validate.mapper.deepAssert

/**
 * @author Peter Gulko (zTrap)
 * @date 11.03.2021
 */
class IterableChecksTest {

    @Test
    fun `test not empty iterable check`() {
        val raw1 = NotEmptyIterableCheckTestCase.Raw(iterable = emptyList())
        val result1 = NotEmptyIterableCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(iterable=[])
                |	Params -> iterable - Reasons -> [{reason="empty iterable"}]
                """.trimMargin()
            )

        val raw2 = NotEmptyIterableCheckTestCase.Raw(iterable = listOf(Unit))
        val result2 = NotEmptyIterableCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.getOrNull()).isNotNull.hasToString("Mapped(iterable=[kotlin.Unit])")
    }

    @Test
    fun `test not blank string iterable check`() {
        val raw1 = NotNullOrBlankStringIterableCheckTestCase.Raw(iterable = emptyList())
        val result1 = NotNullOrBlankStringIterableCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(iterable=[])
                |	Params -> iterable - Reasons -> [{reason="empty iterable"}]
                """.trimMargin()
            )

        val raw2 = NotNullOrBlankStringIterableCheckTestCase.Raw(iterable = listOf(" "))
        val result2 = NotNullOrBlankStringIterableCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(iterable=[ ])
                |	Params -> iterable - Reasons -> [{reason="iterable with only blank strings"}]
                """.trimMargin()
            )

        val raw3 = NotNullOrBlankStringIterableCheckTestCase.Raw(iterable = listOf(null))
        val result3 = NotNullOrBlankStringIterableCheckTestCase.Raw.runCatching { invoke(raw3) }

        assertThat(result3.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(iterable=[null])
                |	Params -> iterable - Reasons -> [{reason="iterable with only nulls"}]
                """.trimMargin()
            )

        val raw4 = NotNullOrBlankStringIterableCheckTestCase.Raw(iterable = listOf(null, " "))
        val result4 = NotNullOrBlankStringIterableCheckTestCase.Raw.runCatching { invoke(raw4) }

        assertThat(result4.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(iterable=[null,  ])
                |	Params -> iterable - Reasons -> [{reason="iterable with only nulls and blank strings"}]
                """.trimMargin()
            )

        val raw5 = NotNullOrBlankStringIterableCheckTestCase.Raw(iterable = listOf("123"))
        val result5 = NotNullOrBlankStringIterableCheckTestCase.Raw.runCatching { invoke(raw5) }

        assertThat(result5.getOrNull()).isNotNull.hasToString("Mapped(iterable=[123])")
    }
}

private object NotEmptyIterableCheckTestCase {

    data class Raw(
        @Checks(NotEmptyIterableCheck::class)
        val iterable: Iterable<Any>?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(raw.iterable.deepAssert())
        }
    }

    data class Mapped(val iterable: List<Any>)
}

private object NotNullOrBlankStringIterableCheckTestCase {

    data class Raw(
        @Checks(NotNullOrBlankStringIterableCheck::class)
        val iterable: Iterable<String?>?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(raw.iterable.deepAssert())
        }
    }

    data class Mapped(val iterable: List<String>)
}