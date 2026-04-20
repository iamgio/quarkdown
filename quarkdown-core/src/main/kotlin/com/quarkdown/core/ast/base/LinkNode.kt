package com.quarkdown.core.ast.base

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.error.ErrorCapableNode
import com.quarkdown.core.context.file.FileSystem

/**
 * A general link node.
 * A link can error for various reasons.
 * For example, a [com.quarkdown.core.ast.base.inline.SubdocumentLink] can error
 * if the linked subdocument cannot be found.
 *
 * @see com.quarkdown.core.ast.base.inline.Link
 * @see com.quarkdown.core.ast.base.block.LinkDefinition
 */
interface LinkNode : ErrorCapableNode {
    /**
     * Inline content of the displayed label.
     */
    val label: InlineContent

    /**
     * URL this link points to.
     */
    val url: String

    /**
     * Optional title.
     */
    val title: InlineContent?

    /**
     * Optional file system where this link is defined, used for resolving relative paths.
     * @see com.quarkdown.core.context.hooks.LinkUrlResolverHook
     */
    val fileSystem: FileSystem?

    /**
     * Creates a copy of this link with the given [url].
     */
    fun copy(url: String): LinkNode
}
