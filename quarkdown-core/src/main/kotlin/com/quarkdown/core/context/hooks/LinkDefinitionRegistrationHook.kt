package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext

/**
 * Hook that registers the [LinkDefinition]s in the [context] so that they can be later looked up
 * by [com.quarkdown.core.ast.base.inline.ReferenceLink]s.
 */
class LinkDefinitionRegistrationHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<LinkDefinition> {
            context.register(it)
        }
    }
}
