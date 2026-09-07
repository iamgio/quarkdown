package com.quarkdown.core.context.options

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.context.Context
import com.quarkdown.core.media.storage.options.MediaStorageOptions

/**
 * Read-only properties that affect several behaviors of the document generation process,
 * and that can be altered through function calls through its [MutableContextOptions] implementation.
 */
interface ContextOptions : MediaStorageOptions {
    /**
     * When a [Heading] node has a depth equals or less than this value, a page break is forced.
     */
    val autoPageBreakHeadingMaxDepth: Int?

    /**
     * Whether automatic identifiers should be generated for elements
     * that do not have an explicit one.
     * For example, a heading element (`# Hello world`) automatically generates
     * an identifier (`hello-world`) that can be referenced by other elements.
     * @see com.quarkdown.core.ast.attributes.id.IdentifierProvider
     */
    val enableAutomaticIdentifiers: Boolean

    /**
     * Whether certain nodes can be aware of their location within the document
     * in order to display it, for example in headings.
     * @see com.quarkdown.core.ast.attributes.location.LocationTrackableNode
     */
    val enableLocationAwareness: Boolean

    /**
     * The suffixes that, if matched by a link's URL, indicates that the link points to a Quarkdown subdocument.
     * @see com.quarkdown.core.ast.base.inline.SubdocumentLink
     */
    val subdocumentUrlSuffixes: Set<String>

    /**
     * Supplier of unique identifiers (UUIDs). For instance, UUIDs are generated for anonymous footnotes.
     */
    val uuidSupplier: () -> String

    /**
     * Native HTML generation options.
     */
    val html: HtmlOptions
}

/**
 * Checks whether the [heading] node should force a page break, depending on:
 * - whether the heading can break a page
 * - if [ContextOptions.autoPageBreakHeadingMaxDepth] is set, whether the heading's depth is <= the maximum depth.
 * - if unset, checks against [com.quarkdown.core.document.DocumentType.preferredAutoPageBreakHeadingMaxDepth].
 * @see ContextOptions.autoPageBreakHeadingMaxDepth
 */
fun Context.shouldAutoPageBreak(heading: Heading): Boolean {
    if (!heading.canBreakPage) return false

    val maxDepth =
        this.options.autoPageBreakHeadingMaxDepth
            ?: this.documentInfo.type.preferredAutoPageBreakHeadingMaxDepth
            ?: return false

    return heading.depth <= maxDepth
}

/**
 * @param url URL or file path to check, without any surrounding whitespace or anchors
 * @return whether the given [url], which may also be a file path, points to a Quarkdown subdocument
 * depending on its suffix (file extension).
 * @see com.quarkdown.core.ast.base.inline.SubdocumentLink
 */
fun Context.isSubdocumentUrl(url: String): Boolean = options.subdocumentUrlSuffixes.any { url.endsWith(it, ignoreCase = true) }
