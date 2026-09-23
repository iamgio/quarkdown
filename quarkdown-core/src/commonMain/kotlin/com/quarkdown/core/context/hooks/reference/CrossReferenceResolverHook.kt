package com.quarkdown.core.context.hooks.reference

import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.context.MutableContext

/**
 * A [ReferenceDefinitionResolverHook] that associates a [CrossReferenceableNode] to each [CrossReference] by means of matching IDs.
 */
class CrossReferenceResolverHook(
    context: MutableContext,
) : ReferenceDefinitionResolverHook<CrossReference, CrossReferenceableNode, CrossReferenceableNode>(context) {
    override fun collectReferences(iterator: ObservableAstIterator) = iterator.collectAll<CrossReference>()

    override fun collectDefinitions(iterator: ObservableAstIterator) = iterator.collectAll<CrossReferenceableNode>()

    override fun findDefinitionPair(
        reference: CrossReference,
        definitions: List<CrossReferenceableNode>,
        index: Int,
    ): Pair<CrossReferenceableNode, CrossReferenceableNode>? =
        definitions
            .find { reference.referenceId == it.referenceId }
            ?.let { it to it }
}
