package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.document.DocumentType
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for lister of enum entries for an enum parameter.
 */
class EnumParameterEntryListerTransformerTest :
    QuarkdocDokkaTest(
        imports =
            listOf(DocumentType::class),
    ) {
    @Test
    fun `enum parameter from same module`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(x: DocumentType) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, "Values")
            assertContains(parameters, "plain")
            assertContains(parameters, "slides")
            assertContains(parameters, "paged")
        }
    }

    @Test
    fun `enum parameter from same module with other parameter`() {
        test(
            """
            /**
             * @param x Test 1
             * @param y Test 2
             */
            fun func(x: DocumentType, y: Int) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, "Test 1")
            assertContains(parameters, "Values")
            assertContains(parameters, "plain")
            assertContains(parameters, "slides")
            assertContains(parameters, "paged")
            assertContains(parameters, "y")
            assertContains(parameters, "Test 2")
        }
    }

    // Could not find a way to unit-test enums from the `core` module.
}
