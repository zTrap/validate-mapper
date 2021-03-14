package ru.ztrap.tools.validate.checks

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.annotations.NotRequired
import ru.ztrap.tools.validate.mapper.ValidateMapper

/**
 * @author Peter Gulko (zTrap)
 * @date 11.03.2021
 */
class CommonChecksTest {

    @Test
    fun `test not required check`() {
        val raw1 = NotRequiredCheckTestCase.Raw(string = null)
        val result1 = NotRequiredCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.getOrNull()).isNotNull.hasToString("Mapped(string=null)")

        val raw2 = NotRequiredCheckTestCase.Raw(string = "")
        val result2 = NotRequiredCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.getOrNull()).isNotNull.hasToString("Mapped(string=)")
    }

    @Test
    fun `test null check`() {
        val raw1 = NullCheckTestCase.Raw(string = null)
        val result1 = NullCheckTestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(string=null)
                |	Params -> string - Reasons -> [{reason="value is null"}]
                """.trimMargin()
            )

        val raw2 = NullCheckTestCase.Raw(string = "")
        val result2 = NullCheckTestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.getOrNull()).isNotNull.hasToString("Mapped(string=)")
    }
}

private object NotRequiredCheckTestCase {

    data class Raw(
        @NotRequired
        val string: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(raw.string)
        }
    }

    data class Mapped(val string: String?)
}

private object NullCheckTestCase {

    data class Raw(
        val string: String?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(requireNotNull(raw.string))
        }
    }

    data class Mapped(val string: String)
}