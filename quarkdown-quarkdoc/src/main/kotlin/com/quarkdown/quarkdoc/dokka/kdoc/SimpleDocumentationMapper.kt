package com.quarkdown.quarkdoc.dokka.kdoc

import org.jetbrains.dokka.model.WithChildren
import kotlin.reflect.KClass

/**
 * A simple implementation of [DocumentationMapper] that performs a shallow mapping of documentation nodes.
 */
class SimpleDocumentationMapper : DocumentationMapper {
    private val mappers = mutableMapOf<KClass<out WithChildren<*>>, (WithChildren<*>) -> WithChildren<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : WithChildren<*>> register(
        nodeType: KClass<T>,
        mapper: (T) -> WithChildren<*>,
    ): DocumentationMapper =
        apply {
            mappers[nodeType] = mapper as (WithChildren<*>) -> WithChildren<*>
        }

    override fun map(node: WithChildren<*>): WithChildren<*> = mappers[node::class]?.let { mapper -> mapper(node) } ?: node
}
