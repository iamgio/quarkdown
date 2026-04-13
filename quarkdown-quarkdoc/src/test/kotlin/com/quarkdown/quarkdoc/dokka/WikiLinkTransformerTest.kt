package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.page.WIKI_ROOT
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for the `@wiki` documentation tag.
 */
class WikiLinkTransformerTest : QuarkdocDokkaTest() {
    @Test
    fun `no wiki`() {
        test(
            """
            /**
             *
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertFalse("Wiki page" in it)
        }
    }

    @Test
    fun wiki() {
        test(
            """
            /**
             * @wiki home
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Wiki page")
            assertContains(it, WIKI_ROOT + "home")
        }
    }

    @Test
    fun `wiki with hyphenated slug`() {
        test(
            """
            /**
             * @wiki multi-column-layout
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Wiki page")
            assertContains(it, WIKI_ROOT + "multi-column-layout")
        }
    }

    @Test
    fun `wiki with anchor`() {
        test(
            """
            /**
             * @wiki file-data#reading-files
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Wiki page")
            assertContains(it, WIKI_ROOT + "file-data#reading-files")
        }
    }
}
