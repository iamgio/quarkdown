package com.quarkdown.quarkdoc.dokka

import kotlin.test.Test

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class ModuleTransformerTest : QuarkdocDokkaTest() {
    @Test
    fun `two files`() {
        test(
            "fun someFunction() = Unit",
            "some-function",
        ) {
            // assertContains(getSignature(it), "fun someFunction()")
        }
    }
}
