package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext

/**
 * Hook that registers the [LinkDefinition]s in the [context] so that they can be later looked up
 * by [eu.iamgio.quarkdown.ast.base.inline.ReferenceLink]s.
 */
class LinkDefinitionRegistrationHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<LinkDefinition> {
            context.register(it)
        }
    }
}
