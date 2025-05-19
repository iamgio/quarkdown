package com.quarkdown.core.flavor

import com.quarkdown.core.flavor.base.BaseMarkdownRendererFactory
import com.quarkdown.core.flavor.quarkdown.QuarkdownRendererFactory

/**
 * Provider of rendering strategies.
 */
interface RendererFactory {
    fun <T> accept(visitor: RendererFactoryVisitor<T>): T
}

interface RendererFactoryVisitor<T> {
    fun visit(factory: BaseMarkdownRendererFactory): T

    fun visit(factory: QuarkdownRendererFactory): T
}
