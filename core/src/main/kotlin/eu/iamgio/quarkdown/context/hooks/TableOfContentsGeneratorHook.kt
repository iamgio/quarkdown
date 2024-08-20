package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.toc.TableOfContents

/**
 * Hook that allows the generation of a [TableOfContents] by iterating through [Heading]s.
 * The [TableOfContents] is stored in the [context]'s [MutableContext.attributes] at the end of the traversal.
 */
class TableOfContentsGeneratorHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        val headings = mutableListOf<Heading>()

        // Collecting headings.
        iterator.on<Heading> { headings += it }

        // Generation.
        iterator.onFinished {
            context.attributes.tableOfContents = TableOfContents.generate(headings.asSequence())
        }
    }
}
