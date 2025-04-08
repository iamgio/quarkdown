package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.ast.quarkdown.block.Math
import eu.iamgio.quarkdown.ast.quarkdown.block.MermaidDiagram
import eu.iamgio.quarkdown.ast.quarkdown.inline.MathSpan
import eu.iamgio.quarkdown.context.MutableContext

/**
 * Hook that marks the presence of code elements in the [context]'s attributes
 * if at least one [Code] block is present in the document.
 */
class CodePresenceHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Code> {
            with(context.attributes) {
                if (!hasCode) hasCode = true
            }
        }
    }
}

/**
 * Hook that marks the presence of code elements in the [context]'s attributes
 * if at least one [Code] block is present in the document.
 */
class MermaidDiagramPresenceHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<MermaidDiagram> {
            with(context.attributes) {
                if (!hasMermaidDiagram) hasMermaidDiagram = true
            }
        }
    }
}

/**
 * Hook that marks the presence of math elements in the [context]'s attributes
 * if at least one [Math] or [MathSpan] block is present in the document.
 */
class MathPresenceHook(
    private val context: MutableContext,
) : AstIteratorHook {
    private val action: (Node) -> Unit
        get() = {
            with(context.attributes) {
                if (!hasMath) hasMath = true
            }
        }

    override fun attach(iterator: ObservableAstIterator) {
        iterator
            .on<Math>(action)
            .on<MathSpan>(action)
    }
}
