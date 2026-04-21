package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.ReferenceNode
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.pipeline.error.PipelineErrorHandler
import com.quarkdown.core.util.stripAnchor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional inline title
 * @param fileSystem optional file system where this link is defined, used for resolving relative paths
 */
class Link(
    override val label: InlineContent,
    override val url: String,
    override val title: InlineContent?,
    override val fileSystem: FileSystem? = null,
) : LinkNode,
    TextNode {
    override val text: InlineContent
        get() = label

    override var error: Pair<Throwable, PipelineErrorHandler>? = null

    override fun <T> acceptOnSuccess(visitor: NodeVisitor<T>) = visitor.visit(this)

    override fun copy(url: String) =
        Link(
            label = label,
            url = url,
            title = title,
            fileSystem = fileSystem,
        )

    /**
     * Strips the anchor (fragment) from the URL.
     * @return a pair of the link with the anchor removed and the anchor itself,
     *         or `null` if no anchor is present
     */
    fun stripAnchor(): Pair<Link, String>? {
        val (url, anchor) = this.url.stripAnchor() ?: return null
        return Pair(copy(url = url), anchor)
    }
}

/**
 * A link that references a [LinkDefinition].
 * @param label inline content of the displayed label
 * @param referenceLabel label of the [LinkDefinition] this link points to
 * @param fallback supplier of the node to show instead of [label] in case the reference is not resolved
 * @param onResolve actions to perform when the reference is resolved.
 * @see com.quarkdown.core.context.hooks.reference.LinkDefinitionResolverHook
 */
class ReferenceLink(
    val label: InlineContent,
    val referenceLabel: InlineContent,
    val fallback: () -> Node,
    val onResolve: MutableList<(resolved: LinkNode) -> Unit> = mutableListOf(),
) : ReferenceNode<ReferenceLink, LinkNode> {
    override val reference: ReferenceLink = this

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
