package ru.ztrap.tools.validate.gson

import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ztrap.tools.validate.checks.ValidateChecker
import ru.ztrap.tools.validate.mapper.ValidateMapper

/**
 * @author pa.gulko zTrap (20.12.2019)
 */
class GsonExtensionTest {

    @Test fun `test gson field name extract`() {
        val config: GsonBuilder.() -> Unit = {
            setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
        }

        val gson = GsonBuilder().apply(config).create()

        ValidateExtensionGson.install(config)

        val raw1 = gson.fromJson(TestCase.json1, TestCase.Raw::class.java)
        val result1 = TestCase.Raw.runCatching { invoke(raw1) }

        assertThat(result1.getOrNull())
            .isNotNull
            .hasToString("Mapped(firstField=string value, secondField=false)")

        val raw2 = gson.fromJson(TestCase.json2, TestCase.Raw::class.java)
        val result2 = TestCase.Raw.runCatching { invoke(raw2) }

        assertThat(result2.exceptionOrNull())
            .isNotNull()
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(firstField=string value, secondField=null)
                |	Params -> second (field: secondField); Reasons -> [null]
                """.trimMargin()
            )

        val raw3 = gson.fromJson(TestCase.json3, TestCase.Raw::class.java)
        val result3 = TestCase.Raw.runCatching { invoke(raw3) }

        assertThat(result3.exceptionOrNull())
            .isNotNull()
            .hasMessage(
                """
                |Failed validation of received object.
                |	Object -> Raw(firstField=string value, secondField=null)
                |	Params -> second (field: secondField); Reasons -> [null]
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
        |""".trimMargin()

    val json2 = """
        |{
        |  "first_field": "string value",
        |  "second_field": false
        |}
        |""".trimMargin()

    val json3 = """
        |{
        |  "first_field": "string value",
        |  "second": null
        |}
        |""".trimMargin()

    data class Raw(
        val firstField: String?,
        @SerializedName("second")
        val secondField: Boolean?
    ) {
        companion object Mapper : ValidateMapper<Raw, Mapped>() {
            override fun transform(raw: Raw) = Mapped(
                raw.firstField!!,
                raw.secondField!!
            )
        }
    }

    data class Mapped(val firstField: String, val secondField: Boolean)

    object TrimmedStringLengthCheck : ValidateChecker() {
        const val LENGTH = "LENGTH"

        override fun invoke(raw: Any, parameters: Map<String, Any>): Result {
            return if (raw is String) {
                val maxLength = parameters[LENGTH]?.toString()?.toLong()
                if (maxLength != null) {
                    if (maxLength >= 0) {
                        val currentLength = raw.trim().length
                        if (currentLength <= maxLength) {
                            Result.Success
                        } else {
                            Result.Error("current length = $currentLength and max length = $maxLength")
                        }
                    } else {
                        Result.Error("max length is negative")
                    }
                } else {
                    Result.Error("length constraint is not set via @CheckParametrized(expressionClass=TrimmedStringLengthCheck::class, long=[LongParameter(name=TrimmedStringLengthCheck.LENGTH, value=0)])")
                }
            } else {
                Result.Error("not a string")
            }
        }
    }
}