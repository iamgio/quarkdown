package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * `@Spread` expansion: the wrapper's parameter list absorbs the class's members and the
 * delegation call reconstructs the class via named-argument constructor invocation.
 */
class SpreadParameterTest {
    private val source by lazy { GeneratedFiles.sourceOf("SpreadParameter") }

    @Test
    fun `outer spread parameter does not appear in the wrapper signature`() {
        // No entry for the outer `style: Style` parameter itself.
        assertFalse(
            "`style`: " in source,
            "outer @Spread parameter must be replaced by its components, not carried through:\n$source",
        )
    }

    @Test
    fun `each spread component becomes a wrapper parameter under its exported name`() {
        // `@Name("foreground") val foregroundColor` -> wrapper param `foreground`
        assertContains(source, "`foreground`: kotlin.String")
        // `@Name("background") val backgroundColor = "white"` -> wrapper param `background`
        assertContains(source, "`background`: kotlin.String = \"white\"")
        // `@LikelyNamed val opacity = 1.0` -> wrapper param `opacity` with propagated annotation
        assertContains(source, "@LikelyNamed `opacity`: kotlin.Double = 1.0")
    }

    @Test
    fun `plain siblings survive alongside spread components in declaration order`() {
        assertContains(source, "`text`: kotlin.String")
        // The spread expansion happens where the outer parameter sat, so the wrapper reads
        // text -> foreground -> background -> opacity in that order.
        val textIdx = source.indexOf("`text`: kotlin.String")
        val foregroundIdx = source.indexOf("`foreground`: kotlin.String")
        val opacityIdx = source.indexOf("`opacity`: kotlin.Double")
        check(textIdx in 0 until foregroundIdx) { "text should appear before spread components" }
        check(foregroundIdx in 0 until opacityIdx) { "spread components should be in declaration order" }
    }

    @Test
    fun `delegation reconstructs the data class via named-argument constructor call`() {
        // outer name -> original data-class-parameter names -> exported wrapper param names
        assertContains(
            source,
            "`style` = com.quarkdown.processor.fixtures.`Style`(" +
                "`foregroundColor` = `foreground`, " +
                "`backgroundColor` = `background`, " +
                "`opacity` = `opacity`, " +
                "`color` = `color`)",
        )
    }

    @Test
    fun `spread component default referencing a renamed sibling is rewritten to the exported name`() {
        // Source: `val color: String = foregroundColor` where `foregroundColor` was renamed
        // to `foreground` via `@Name`. The wrapper's default must track the rename.
        assertContains(source, "`color`: kotlin.String = foreground")
    }

    @Test
    fun `Spread marker itself is filtered from the wrapper`() {
        assertFalse("@Spread" in source, "@Spread should not leak into the generated wrapper")
    }
}
