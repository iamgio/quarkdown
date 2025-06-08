package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for the *Chaining* section transformer.
 */
class LikelyChainedTransformerTest :
    QuarkdocDokkaTest(
        stringImports = listOf(LikelyChained::class.qualifiedName!!, LikelyChained::class.qualifiedName!!),
        stringPaths = listOf(LikelyChained::class.java.packageName + ".QuarkdocAnnotations"),
    ) {
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
            assertFalse("Chaining" in it)
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
            assertContains(it, "Chaining")
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
            assertContains(it, "Chaining")
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
            assertContains(it, "Chaining")
            assertContains(getText(it), "Int::func b:{String?}") // Default value is not shown.
        }
    }
}
