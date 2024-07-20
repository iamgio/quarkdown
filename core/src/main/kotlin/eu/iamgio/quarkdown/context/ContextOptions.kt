package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.Heading

/**
 * Read-only properties that affect several behaviors,
 * and that can be altered through function calls through its [MutableContextOptions] implementation.
 */
interface ContextOptions {
    /**
     * When a [Heading] node has a depth equals or less than this value, a page break is forced.
     */
    val autoPageBreakHeadingDepth: Int
}

/**
 * @return whether the [heading] node should force a page break
 * @see ContextOptions.autoPageBreakHeadingDepth
 */
fun Context.shouldAutoPageBreak(heading: Heading) = heading.depth <= this.options.autoPageBreakHeadingDepth

/**
 * Mutable [ContextOptions] implementation.
 */
data class MutableContextOptions(
    override var autoPageBreakHeadingDepth: Int = 1,
) : ContextOptions
