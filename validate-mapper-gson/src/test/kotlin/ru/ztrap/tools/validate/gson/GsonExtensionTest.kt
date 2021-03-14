package ru.ztrap.tools.validate.gson

import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.mapper.ValidateMapper

/**
 * @author pa.gulko zTrap (20.12.2019)
 */
class GsonExtensionTest {

    @Test fun `test gson field name extract`() {
        val gson = GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create()

        ValidateExtensionGson.install(gson)

        val raw1 = gson.fromJson(TestCase.json1, TestCase.Raw::class.java)
        val result1 = TestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.getOrNull())
            .isNotNull
            .hasToString("Mapped(firstField=string value, secondField=false)")

        val raw2 = gson.fromJson(TestCase.json2, TestCase.Raw::class.java)
        val result2 = TestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(firstField=string value, secondField=null)
                |	Params -> second (field: secondField) - Reasons -> [{reason="value is null"}]
                """.trimMargin()
            )

        val raw3 = gson.fromJson(TestCase.json3, TestCase.Raw::class.java)
        val result3 = TestCase.Raw.runCatching { invoke(raw3) }

        assertThat(result3.exceptionOrNull())
            .isNotNull
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(firstField=null, secondField=null)
                |	Params -> first_field (field: firstField) - Reasons -> [{reason="value is null"}],
                |	          second (field: secondField) ----- Reasons -> [{reason="value is null"}]
                """.trimMargin()
            )
    }
}

object TestCase {
    val json1 = """
        |{
        |  "first_field": "string value",
        |  "second": false
        |}
        """.trimMargin()

    val json2 = """
        |{
        |  "first_field": "string value",
        |  "second_field": false
        |}
        """.trimMargin()

    val json3 = """
        |{
        |  "first_field": null,
        |  "second": null
        |}
        """.trimMargin()

    data class Raw(
        val firstField: String?,
        @SerializedName("second")
        val secondField: Boolean?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(
                requireNotNull(raw.firstField),
                requireNotNull(raw.secondField)
            )
        }
    }

    data class Mapped(val firstField: String, val secondField: Boolean)
}