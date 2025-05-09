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
            assertContains(it, "Return")
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
            assertContains(it, "Return")
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
            assertContains(it, "Return")
            assertContains(it, "Target")
            assertContains(it, "paged")
            assertContains(it, "slides")
        }
    }
}
