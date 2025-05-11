package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.Injected
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class SuppressInjectedTransformerTest : QuarkdocDokkaTest(imports = listOf(Injected::class)) {
    @Test
    fun `injected single parameter`() {
        test(
            "fun someFunction(@Injected x: Int) = Unit",
            "some-function",
        ) {
            assertContains(getSignature(it), "fun someFunction()")
        }
    }

    @Test
    fun `injected first parameter`() {
        test(
            "fun someFunction(@Injected x: Int, y: Int) = Unit",
            "some-function",
        ) {
            assertContains(getSignature(it), "fun someFunction(y: Int)")
        }
    }
}
