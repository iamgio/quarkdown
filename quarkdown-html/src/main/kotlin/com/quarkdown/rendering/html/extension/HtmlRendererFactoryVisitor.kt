package com.quarkdown.rendering.html.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactoryVisitor
import com.quarkdown.core.flavor.base.BaseMarkdownRendererFactory
import com.quarkdown.core.flavor.quarkdown.QuarkdownRendererFactory
import com.quarkdown.core.rendering.NodeRenderer
import com.quarkdown.core.rendering.html.BaseHtmlNodeRenderer
import com.quarkdown.rendering.html.node.QuarkdownHtmlNodeRenderer

/**
 * Supplier of an HTML node renderer from the active renderer factory.
 */
class HtmlRendererFactoryVisitor(
    private val context: Context,
) : RendererFactoryVisitor<NodeRenderer> {
    override fun visit(factory: BaseMarkdownRendererFactory) = BaseHtmlNodeRenderer(context)

    override fun visit(factory: QuarkdownRendererFactory) = QuarkdownHtmlNodeRenderer(context)
}
