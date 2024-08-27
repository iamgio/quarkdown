package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.base.block.Heading

/**
 * Read-only properties that affect several behaviors,
 * and that can be altered through function calls through its [MutableContextOptions] implementation.
 */
interface ContextOptions {
    /**
     * When a [Heading] node has a depth equals or less than this value, a page break is forced.
     */
    val autoPageBreakHeadingDepth: Int

    /**
     * Whether automatic identifiers should be generated for elements
     * that do not have an explicit one.
     * For example, a heading element (`# Hello world`) automatically generates
     * an identifier (`hello-world`) that can be referenced by other elements.
     */
    val enableAutomaticIdentifiers: Boolean

    /**
     * Whether media storage should be enabled.
     * If enabled, media objects referenced in the document are copied to the output directory
     * and those elements that use them (e.g. images) automatically reference the new local path.
     * This doesn't take effect (= disabled) with the base Markdown flavor, as the media architecture is defined by Quarkdown.
     */
    val enableMediaStorage: Boolean
}

/**
 * @return whether the [heading] node should force a page break
 * @see ContextOptions.autoPageBreakHeadingDepth
 */
fun Context.shouldAutoPageBreak(heading: Heading) = !heading.isMarker && heading.depth <= this.options.autoPageBreakHeadingDepth

/**
 * Mutable [ContextOptions] implementation.
 */
data class MutableContextOptions(
    override var autoPageBreakHeadingDepth: Int = 1,
    override var enableAutomaticIdentifiers: Boolean = true,
    override var enableMediaStorage: Boolean = true,
) : ContextOptions
