package com.quarkdown.quarkdoc.dokka.kdoc

import com.quarkdown.quarkdoc.dokka.util.tryCopy
import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.doc.DocTag
import kotlin.reflect.KClass

/**
 * An implementation of [DocumentationMapper] that performs a deep mapping of documentation nodes down the node tree.
 */
class DeepDocumentationMapper : DocumentationMapper {
    private val simple = SimpleDocumentationMapper()

    override fun <T : WithChildren<*>> register(
        nodeType: KClass<T>,
        mapper: (T) -> WithChildren<*>,
    ): DocumentationMapper =
        apply {
            simple.register(nodeType, mapper)
        }

    @Suppress("UNCHECKED_CAST")
    override fun map(node: WithChildren<*>): WithChildren<*> =
        simple.map(node).let { mapped ->
            val children = mapped.children as List<WithChildren<*>>
            mapped.tryCopy(newChildren = children.map(::map) as List<DocTag>)
        }
}
