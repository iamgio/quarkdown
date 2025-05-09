package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents

/**
 * Hook that allows the generation of a [TableOfContents] by iterating through [Heading]s.
 * The [TableOfContents] is stored in the [context]'s [MutableContext.attributes] at the end of the traversal.
 */
class TableOfContentsGeneratorHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        val headings = iterator.collect<Heading> { it.canTrackLocation }

        // Generation.
        iterator.onFinished {
            context.attributes.tableOfContents = TableOfContents.generate(headings.asSequence())
        }
    }
}
