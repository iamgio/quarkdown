package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

private const val OPTIONAL_TEXT = "Optional"
private const val BODY_TEXT = "Likely passed as a body argument"

/**
 * Tests for [com.quarkdown.quarkdoc.dokka.transformers.optional.AdditionalParameterPropertiesTransformer].
 */
class AdditionalParameterPropertiesTransformerTest : QuarkdocDokkaTest(imports = listOf(LikelyBody::class)) {
    @Test
    fun `no additional properties`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(x: Int) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertFalse(OPTIONAL_TEXT in parameters)
            assertFalse(BODY_TEXT in parameters)
        }
    }

    @Test
    fun `only optional parameter`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(x: Int = 0) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, OPTIONAL_TEXT)
        }
    }

    @Test
    fun `body parameter`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(@LikelyBody x: Int) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, BODY_TEXT)
            assertFalse(OPTIONAL_TEXT in parameters)
        }
    }

    @Test
    fun `body and optional parameter`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(@LikelyBody x: Int = 0) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, BODY_TEXT)
            assertContains(parameters, OPTIONAL_TEXT)
        }
    }
}
