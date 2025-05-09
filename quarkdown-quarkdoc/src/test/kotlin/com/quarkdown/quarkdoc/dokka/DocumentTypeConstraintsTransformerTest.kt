package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class DocumentTypeConstraintsTransformerTest :
    QuarkdocDokkaTest(
        imports =
            listOf(
                DocumentType::class,
                OnlyForDocumentType::class,
            ),
    ) {
    private fun assertContainsNormalGeneration(output: String) {
        assertContains(output, "Paragraph 1.")
        assertContains(output, "Paragraph 2.")
        assertContains(output, "Return")
    }

    @Test
    fun `no constraint`() {
        test(
            """
            /**
             * Paragraph 1.
             *
             * Paragraph 2.
             *
             * @return test
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContainsNormalGeneration(it)
            assertFalse("Target" in it)
        }
    }

    @Test
    fun `only for type`() {
        test(
            """
            /**
             * Paragraph 1.
             *
             * Paragraph 2.
             *
             * @return test
             */
            @OnlyForDocumentType(DocumentType.PAGED)
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContainsNormalGeneration(it)
            assertContains(it, "Target")
            assertContains(it, "paged")
        }
    }

    @Test
    fun `only for two types`() {
        test(
            """
            /**
             * Paragraph 1.
             *
             * Paragraph 2.
             *
             * @return test
             */
            @OnlyForDocumentType(DocumentType.PAGED, DocumentType.SLIDES)
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContainsNormalGeneration(it)
            assertContains(it, "Target")
            assertContains(it, "paged")
            assertContains(it, "slides")
        }
    }
}
