package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.flavor.RendererFactoryVisitor

/**
 * [QuarkdownRendererFactory] renderer factory.
 */
class QuarkdownRendererFactory : RendererFactory {
    override fun <T> accept(visitor: RendererFactoryVisitor<T>): T = visitor.visit(this)
}
