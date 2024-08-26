package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext

/**
 * Hook that marks the presence of code elements in the [context]'s attributes
 * if at least one [Code] block is present in the document.
 */
class CodePresenceHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Code> {
            with(context.attributes) {
                if (!hasCode) hasCode = true
            }
        }
    }
}
