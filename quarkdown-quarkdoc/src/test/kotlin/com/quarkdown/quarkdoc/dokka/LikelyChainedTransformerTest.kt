package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.quarkdoc.reader.anchors.Anchors
import com.quarkdown.quarkdoc.reader.anchors.AnchorsHtml
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CHAINING_TEXT = "Chaining"

/**
 * Tests for the *Chaining* section transformer.
 */
class LikelyChainedTransformerTest :
    QuarkdocDokkaTest(
        stringImports = listOf(LikelyChained::class.qualifiedName!!, LikelyChained::class.qualifiedName!!),
        stringPaths = listOf(LikelyChained::class.java.packageName + ".QuarkdocAnnotations"),
    ) {
    private fun containsAnchor(html: String) = AnchorsHtml.toAnchorAttribute(Anchors.LIKELY_CHAINED) in html

    @Test
    fun `not chained`() {
        test(
            """
            /**
             *
             */
            fun func(a: Int, b: String) = Unit
            """.trimIndent(),
            "func",
        ) {
            assertFalse(CHAINING_TEXT in it)
            assertFalse(containsAnchor(it))
        }
    }

    @Test
    fun `chained, two parameters`() {
        test(
            """
            /**
             * 
             */
            @LikelyChained
            fun func(a: Int, b: String) = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, CHAINING_TEXT)
            assertTrue(containsAnchor(it))
            assertTrue(containsAnchor(it))
            assertContains(getText(it), "Int::func b:{String}")
        }
    }

    @Test
    fun `chained, one parameter`() {
        test(
            """
            /**
             * 
             */
            @LikelyChained
            fun func(a: Int) = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, CHAINING_TEXT)
            assertTrue(containsAnchor(it))
            assertContains(getText(it), "Int::func")
        }
    }

    @Test
    fun `chained, optional parameter`() {
        test(
            """
            /**
             * 
             */
            @LikelyChained
            fun func(a: Int, b: String? = null) = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, CHAINING_TEXT)
            assertTrue(containsAnchor(it))
            assertContains(getText(it), "Int::func b:{String?}") // Default value is not shown.
        }
    }
}
