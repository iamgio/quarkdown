package com.quarkdown.quarkdoc.dokka

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for [com.quarkdown.quarkdoc.dokka.transformers.optional.AdditionalParameterPropertiesTransformer] that
 * shows whether a parameter is optional.
 */
class OptionalParameterDisplayerTransformerTest : QuarkdocDokkaTest() {
    @Test
    fun `no optional parameters`() {
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
            assertFalse("Optional" in parameters)
        }
    }

    @Test
    fun `only parameter`() {
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
            assertContains(parameters, "Optional")
        }
    }
}
