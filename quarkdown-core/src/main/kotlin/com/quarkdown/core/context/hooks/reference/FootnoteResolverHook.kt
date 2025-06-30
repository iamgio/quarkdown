package com.quarkdown.core.context.hooks.reference

import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.setIndex
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext

/**
 * Hook that associates a [FootnoteDefinition] to each [ReferenceFootnote].
 */
class FootnoteResolverHook(
    context: MutableContext,
) : ReferenceDefinitionResolverHook<ReferenceFootnote, FootnoteDefinition, FootnoteDefinition>(context) {
    override fun collectReferences(iterator: ObservableAstIterator) = iterator.collectAll<ReferenceFootnote>()

    override fun collectDefinitions(iterator: ObservableAstIterator) = iterator.collectAll<FootnoteDefinition>()

    override fun findDefinitionPair(
        reference: ReferenceFootnote,
        definitions: List<FootnoteDefinition>,
        index: Int,
    ): Pair<FootnoteDefinition, FootnoteDefinition>? =
        definitions
            .find { it.label == reference.label }
            .also { it?.setIndex(context, index) }
            ?.let { it to it }
}
