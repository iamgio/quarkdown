package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.id.setIdentifierDeduplicationIndex
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.util.node.toPlainText
import com.quarkdown.core.util.sanitizeAsIdentifier
import com.quarkdown.core.util.toUriIdentifier

/**
 * Hook that scans the document for [Heading]s sharing the same base identifier
 * and stores a deterministic, document-order occurrence index for each of them.
 *
 * The first heading for a given base identifier gets index `0` (no disambiguation needed);
 * each subsequent collision gets `1`, `2`, ... Renderers can use this index to produce
 * unique anchor identifiers.
 *
 * @see com.quarkdown.core.ast.attributes.id.HeadingIdentifierDeduplicationIndexProperty
 */
class HeadingIdentifierDeduplicationHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        // Counts how many headings have produced each base identifier so far, in document order.
        val occurrences = mutableMapOf<String, Int>()

        iterator.on<Heading> { heading ->
            val baseId =
                (heading.customId ?: heading.text.toPlainText().toUriIdentifier())
                    .sanitizeAsIdentifier()
            val index = occurrences.getOrDefault(baseId, 0)
            occurrences[baseId] = index + 1

            if (index > 0) {
                heading.setIdentifierDeduplicationIndex(context, index)
            }
        }
    }
}
