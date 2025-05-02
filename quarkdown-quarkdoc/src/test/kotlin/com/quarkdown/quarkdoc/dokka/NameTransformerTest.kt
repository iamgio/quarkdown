package com.quarkdown.quarkdoc.dokka

import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class NameTransformerTest : QuarkdocDokkaTest() {
    @Test
    fun `no name transformation`() {
        test(
            "fun someFunction() = Unit",
            "some-function",
        ) {
            assertContains(it, "someFunction")
        }
    }

    @Test
    fun `function name transformation`() {
        test(
            """
            @Name("newname")
            fun someFunction() = Unit
            """.trimIndent(),
            "newname",
        ) {
            assertContains(it, "newname")
            // assertFalse("someFunction" in it)
        }
    }
}
