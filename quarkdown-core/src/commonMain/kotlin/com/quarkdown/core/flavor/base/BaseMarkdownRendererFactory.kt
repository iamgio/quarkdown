package com.quarkdown.core.flavor.base

import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.flavor.RendererFactoryVisitor

/**
 * [BaseMarkdownFlavor] renderer factory.
 */
data object BaseMarkdownRendererFactory : RendererFactory {
    override fun <T> accept(visitor: RendererFactoryVisitor<T>): T = visitor.visit(this)
}
