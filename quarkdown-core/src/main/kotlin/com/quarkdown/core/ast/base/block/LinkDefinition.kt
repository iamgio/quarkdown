package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.pipeline.error.PipelineErrorHandler
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creation of a referenceable link definition.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional inline title
 * @param fileSystem optional file system this link is relative to
 */
class LinkDefinition(
    override val label: InlineContent,
    override val url: String,
    override val title: InlineContent?,
    override val fileSystem: FileSystem? = null,
) : LinkNode,
    TextNode {
    override var error: Pair<Throwable, PipelineErrorHandler>? = null

    override fun <T> acceptOnSuccess(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Alias for [label].
     */
    override val text: InlineContent
        get() = label

    override fun copy(url: String) =
        LinkDefinition(
            label = label,
            url = url,
            title = title,
            fileSystem = fileSystem,
        )
}
