package com.quarkdown.quarkdoc.dokka.kdoc

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.doc.DocumentationNode
import kotlin.reflect.KClass

typealias DokkaDocumentation = Map<DokkaConfiguration.DokkaSourceSet, DocumentationNode>

/**
 * A fluent mapper for transforming documentation nodes.
 */
interface DocumentationMapper {
    /**
     * Registers a mapper for a specific type of documentation node.
     * @param nodeType the type of the documentation node to register the mapper for
     * @param mapper the mapper function to apply to the documentation node
     * @return this instance for chaining
     */
    fun <T : WithChildren<*>> register(
        nodeType: KClass<T>,
        mapper: (T) -> WithChildren<*>,
    ): DocumentationMapper

    /**
     * Maps a documentation node using the registered mappers.
     * @param node the documentation node to map
     * @return the mapped documentation node
     */
    fun map(node: WithChildren<*>): WithChildren<*>

    /**
     * Maps a full documentation using the registered mappers.
     * @param documentation the documentation node to map
     * @return the mapped documentation
     */
    fun map(documentation: DokkaDocumentation): DokkaDocumentation =
        documentation
            .mapValues { (_, node) ->
                map(node) as DocumentationNode
            }.toMap()
}
