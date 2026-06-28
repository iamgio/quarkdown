package com.quarkdown.core.ast.base.inline

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.attributes.error.ErrorCapableNode
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An image.
 * @param link the link the image points to
 * @param width optional width constraint
 * @param height optional height constraint
 * @param referenceId optional ID that can be cross-referenced via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 * @param usesMediaStorage whether this image should be registered in the media storage by [com.quarkdown.core.context.hooks.MediaStorerHook],
 *                         if the media storage is enabled in the context
 */
class Image(
    @Diverge val link: LinkNode,
    val width: Size?,
    val height: Size?,
    override val referenceId: String? = null,
    val usesMediaStorage: Boolean = true,
) : CrossReferenceableNode,
    ErrorCapableNode,
    PrimitiveFunctionBackedNode {
    /**
     * Any error associated with the link will be surfaced as an error on the image itself.
     */
    override var error by link::error

    override fun <T> acceptOnSuccess(visitor: NodeVisitor<T>) = visitor.visit(this)

    override val backingFunctionName = "image"

    override fun toFunctionCallArguments() =
        listOf(
            FunctionCallArgument(name = "url", expression = link.url.wrappedAsValue()),
            FunctionCallArgument(name = "label", expression = InlineMarkdownContent(link.label).wrappedAsValue()),
            FunctionCallArgument(
                name = "title",
                expression = link.title?.let(::InlineMarkdownContent)?.wrappedAsValue() ?: NoneValue,
            ),
            FunctionCallArgument(name = "width", expression = width?.let(::ObjectValue) ?: NoneValue),
            FunctionCallArgument(name = "height", expression = height?.let(::ObjectValue) ?: NoneValue),
            FunctionCallArgument(name = "ref", expression = referenceId?.wrappedAsValue() ?: NoneValue),
            FunctionCallArgument(name = "mediastorage", expression = usesMediaStorage.wrappedAsValue()),
            // Suppress the default Figure wrap — the parser already wraps the Image in an
            // ImageFigure when relevant, so .super must return a bare Image to avoid double-figuring.
            FunctionCallArgument(name = "figure", expression = BooleanValue(false)),
        )
}

/**
 * An images that references a [LinkDefinition].
 * @param link the link the image references
 * @param width optional width constraint
 * @param height optional height constraint
 * @param referenceId optional ID that can be cross-referenced via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 */
class ReferenceImage(
    val link: ReferenceLink,
    val width: Size?,
    val height: Size?,
    override val referenceId: String? = null,
) : CrossReferenceableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
