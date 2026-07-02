package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertContains

/** Default-value forwarding, including rewriting of renamed-parameter references. */
class DefaultValueTest {
    @Test
    fun `literal defaults are copied verbatim onto the wrapper parameter`() {
        val source = GeneratedFiles.sourceOf("Defaults")
        assertContains(source, "`prefix`: kotlin.String = \"[quarkdown]\"")
        assertContains(source, "`repeat`: kotlin.Int = 1")
    }

    @Test
    fun `default referencing a renamed sibling parameter is rewritten to the exported name`() {
        val source = GeneratedFiles.sourceOf("Defaults")
        // Source: `@Name("primary") first: String, second: String = first`
        // Wrapper: `primary: kotlin.String, second: kotlin.String = primary`
        assertContains(source, "`second`: kotlin.String = primary")
    }
}
