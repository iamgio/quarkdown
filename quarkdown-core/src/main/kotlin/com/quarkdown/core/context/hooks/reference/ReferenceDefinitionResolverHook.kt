package com.quarkdown.core.context.hooks.reference

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.ReferenceNode
import com.quarkdown.core.ast.attributes.reference.setDefinition
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext

/**
 * Extensible hook that attempts associating a definition to each [ReferenceNode].
 * For example, footnotes, bibliography citations, etc. can be resolved to their definitions.
 * @param R the type of the reference element (for example [com.quarkdown.core.ast.base.inline.ReferenceFootnote])
 * @param DN the type of the node that carries the definition (for example [com.quarkdown.core.ast.base.block.FootnoteDefinition])
 * @param D the type of the definition to be ultimately associated with the reference
 * @see ReferenceNode
 */
abstract class ReferenceDefinitionResolverHook<R, DN : Node, D>(
    protected val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        val references = collectReferences(iterator)
        val definitions = collectDefinitions(iterator)

        iterator.onFinished {
            indexReferences(references).forEach { (index, reference) ->
                val definition = findDefinitionPair(reference.reference, definitions, index)
                definition?.let {
                    reference.setDefinition(context, transformDefinitionPair(it))
                }
            }
        }
    }

    /**
     * @return all [ReferenceNode]s of the desired expected type from the AST.
     */
    protected abstract fun collectReferences(iterator: ObservableAstIterator): List<ReferenceNode<R, D>>

    /**
     * @return all definition nodes of the desired expected type from the AST.
     */
    protected abstract fun collectDefinitions(iterator: ObservableAstIterator): List<DN>

    /**
     * Assigns an index to each reference by their order in the list.
     * The result indices will be input to [findDefinitionPair].
     * @param references the list of references to index
     * @return an iterable of indexed values, where each value is a pair of index and reference node
     */
    protected open fun indexReferences(references: List<ReferenceNode<R, D>>): Iterable<IndexedValue<ReferenceNode<R, D>>> =
        references.withIndex()

    /**
     * Given a reference and a list of definitions to search in, looks for a matching definition.
     * @param reference the reference to find the definition for
     * @param definitions the list of all definitions to search in
     * @param index the index of the reference among all references of the same type, obtained from [indexReferences]
     * @return a pair of a definition node and the definition itself, if found
     */
    protected abstract fun findDefinitionPair(
        reference: R,
        definitions: List<DN>,
        index: Int,
    ): Pair<DN, D>?

    /**
     * Transforms the definition pair into the final definition to be associated with the reference.
     * This is useful if the definition node and the definition itself are not of the same type.
     * @param definition the pair of a definition node and the definition itself
     * @return the definition to be associated with the reference
     */
    protected open fun transformDefinitionPair(definition: Pair<DN, D>): D = definition.second
}
