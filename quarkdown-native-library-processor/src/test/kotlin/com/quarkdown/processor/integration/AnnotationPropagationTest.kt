package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

/** Source-level annotations reach the wrapper; `@Name` and `@QFunction` are filtered out. */
class AnnotationPropagationTest {
    private val source by lazy { GeneratedFiles.sourceOf("Annotations") }

    @Test
    fun `function-level annotations propagate to the wrapper`() {
        assertContains(source, "@LikelyChained")
    }

    @Test
    fun `parameter-level annotations propagate to the wrapper`() {
        assertContains(source, "@Injected `context`")
        assertContains(source, "@LikelyNamed `text`")
    }

    @Test
    fun `annotations on a renamed parameter stay with the renamed identity`() {
        // `@Name("count") @LikelyNamed size` becomes `@LikelyNamed count` in the wrapper.
        assertContains(source, "@LikelyNamed `count`")
    }

    @Test
    fun `Name is filtered from the wrapper because renaming happens at the symbol level`() {
        assertTrue("@Name(" !in source, "@Name should not appear in the wrapper")
    }

    @Test
    fun `QFunction is filtered from the wrapper because it is only a processor marker`() {
        assertTrue("@QFunction" !in source, "@QFunction should not appear in the wrapper")
    }
}
