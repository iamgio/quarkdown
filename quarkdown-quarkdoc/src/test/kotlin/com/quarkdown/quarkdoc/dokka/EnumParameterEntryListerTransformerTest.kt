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

    // Could not find a way to unit-test enums from the `core` module.
}
