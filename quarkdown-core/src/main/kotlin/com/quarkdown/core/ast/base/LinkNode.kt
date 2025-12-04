package com.quarkdown.core.ast.base

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.context.file.FileSystem

/**
 * A general link node.
 * @see com.quarkdown.core.ast.base.inline.Link
 * @see com.quarkdown.core.ast.base.block.LinkDefinition
 */
interface LinkNode : Node {
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
    val title: String?

    /**
     * Optional file system where this link is defined, used for resolving relative paths.
     * @see com.quarkdown.core.context.hooks.LinkUrlResolverHook
     */
    val fileSystem: FileSystem?
}
