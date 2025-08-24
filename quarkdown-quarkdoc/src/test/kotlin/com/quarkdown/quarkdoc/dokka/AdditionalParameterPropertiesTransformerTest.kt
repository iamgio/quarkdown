package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.quarkdoc.reader.anchors.Anchors
import com.quarkdown.quarkdoc.reader.anchors.AnchorsHtml
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val OPTIONAL_TEXT = "Optional"
private const val BODY_TEXT = "Likely a body argument"
private const val NAMED_TEXT = "Likely named"

/**
 * Tests for [com.quarkdown.quarkdoc.dokka.transformers.optional.AdditionalParameterPropertiesTransformer].
 */
class AdditionalParameterPropertiesTransformerTest :
    QuarkdocDokkaTest(
        stringImports = listOf(LikelyBody::class.qualifiedName!!, LikelyNamed::class.qualifiedName!!),
        stringPaths = listOf(LikelyBody::class.java.packageName + ".QuarkdocAnnotations"),
    ) {
    private fun containsAnchor(
        html: String,
        anchor: String,
    ): Boolean = AnchorsHtml.toAnchorAttribute(anchor) in html

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
            assertFalse(containsAnchor(it, Anchors.LIKELY_NAMED))
            assertFalse(containsAnchor(it, Anchors.LIKELY_BODY))
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
            assertTrue(containsAnchor(it, Anchors.OPTIONAL))
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
            assertTrue(containsAnchor(it, Anchors.LIKELY_BODY))
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
            assertTrue(containsAnchor(it, Anchors.LIKELY_BODY))
            assertTrue(containsAnchor(it, Anchors.OPTIONAL))
        }
    }

    @Test
    fun `likely named and optional parameter`() {
        test(
            """
            /**
             * @param x Test
             */
            fun func(@LikelyNamed x: Int = 0) = Unit
            """.trimIndent(),
            "func",
        ) {
            val parameters = getParametersTable(it).text()
            assertContains(parameters, "x")
            assertContains(parameters, NAMED_TEXT)
            assertContains(parameters, OPTIONAL_TEXT)
            assertTrue(containsAnchor(it, Anchors.LIKELY_NAMED))
            assertTrue(containsAnchor(it, Anchors.OPTIONAL))
        }
    }
}
