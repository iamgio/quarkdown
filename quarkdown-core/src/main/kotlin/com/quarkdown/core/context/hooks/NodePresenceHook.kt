package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.MutableContext

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
