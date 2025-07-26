package com.quarkdown.core.context

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.media.storage.options.MediaStorageOptions

private const val DEFAULT_SUBDOCUMENT_URL_SUFFIX = ".qd"

/**
 * Read-only properties that affect several behaviors of the document generation process,
 * and that can be altered through function calls through its [MutableContextOptions] implementation.
 */
interface ContextOptions : MediaStorageOptions {
    /**
     * When a [Heading] node has a depth equals or less than this value, a page break is forced.
     */
    val autoPageBreakHeadingMaxDepth: Int

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
     * The suffix that, if matched by a link's URL, indicates that the link points to a Quarkdown subdocument.
     * @see com.quarkdown.core.ast.base.inline.SubdocumentLink
     */
    val subdocumentUrlSuffix: String

    /**
     * Supplier of unique identifiers (UUIDs). For instance, UUIDs are generated for anonymous footnotes.
     */
    val uuidSupplier: () -> String
}

/**
 * @return whether the [heading] node should force a page break
 * @see ContextOptions.autoPageBreakHeadingMaxDepth
 */
fun Context.shouldAutoPageBreak(heading: Heading) =
    !heading.isMarker &&
        !heading.isDecorative &&
        heading.depth <= this.options.autoPageBreakHeadingMaxDepth

/**
 * Mutable [ContextOptions] implementation.
 */
data class MutableContextOptions(
    override var autoPageBreakHeadingMaxDepth: Int = 1,
    override var enableAutomaticIdentifiers: Boolean = true,
    override var enableLocationAwareness: Boolean = true,
    override var subdocumentUrlSuffix: String = DEFAULT_SUBDOCUMENT_URL_SUFFIX,
    override var uuidSupplier: () -> String = {
        java.util.UUID
            .randomUUID()
            .toString()
    },
    override var enableRemoteMediaStorage: Boolean = false,
    override var enableLocalMediaStorage: Boolean = false,
) : ContextOptions {
    /**
     * Mutates this instance by merging the current media storage rules with the given [options].
     * An option is overridden and merged only if its value from [options] is set, i.e. not `null`.
     * @param options options to merge this instance with
     */
    fun mergeMediaStorageOptions(options: MediaStorageOptions) {
        options.enableRemoteMediaStorage?.let { enableRemoteMediaStorage = it }
        options.enableLocalMediaStorage?.let { enableLocalMediaStorage = it }
    }
}
