package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.ast.attributes.LocationTrackableNode
import com.quarkdown.core.ast.attributes.SectionLocation
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.numbering.NumberingFormat

/**
 * Hook that, if executed after [LocationAwarenessHook] has populated the location data for each location-trackable node,
 * assigns identifiers to elements based on their location in the document and the current [NumberingFormat] which dictates the 'accuracy' or threshold.
 *
 * For example, given a document with the following structure:
 *
 * ```
 * # Heading 1
 *
 * ![Figure](image.png)
 *
 * ## Heading 2
 *
 * ![Another figure](another-image.png)
 *
 * # Heading 3
 *
 * ![Yet another figure](yet-another-image.png)
 * ```
 *
 * - If the numbering format for figures is `1.1`, the first figure will be labeled as `1.1`,
 *   the second as `1.2`, and the third as `2.1`.
 *
 * - If the numbering format for figures is `1`, the first figure will be labeled as `1`,
 *   the second as `2`, and the third as `3`.
 *
 * @see AstAttributes.positionalLabels
 * @see AstAttributes.locations
 * @see LocationTrackableNode
 */
class LocationAwareLabelStorerHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        updateLabels<Figure<*>>(DocumentNumbering::figures, iterator, filter = { it.caption != null })
        updateLabels<Table>(DocumentNumbering::tables, iterator, filter = { it.caption != null })

        // Updates the labels of Numbered nodes, which are grouped by their key.
        // 'extra' numbering formats can be set via the `.numbering` function.
        context.documentInfo.numberingOrDefault?.extra?.forEach { (name, numbering) ->
            updateLabels<Numbered>({ numbering }, iterator, filter = { it.key == name })
        }
    }

    /**
     * Updates labels of elements of type [T] based on their location in the document and the given numbering format.
     * @param formatSupplier numbering format used to generate the labels for this type of element,
     * supplied by [context]'s document numbering settings
     * @param iterator iterator to attach the hook to
     * @param filter condition to satisfy in order to update the label of each element and increment the counter
     * @param T type of elements to update the labels of
     */
    private inline fun <reified T : LocationTrackableNode> updateLabels(
        formatSupplier: (DocumentNumbering) -> NumberingFormat?,
        iterator: ObservableAstIterator,
        crossinline filter: (T) -> Boolean = { true },
    ) {
        // Gets the needed numbering format from the global numbering settings.
        val format = formatSupplier(context.documentInfo.numberingOrDefault ?: return)

        if (format == null || format.isNonCounting) return

        // Stores the number of elements encountered at each location.
        val countAtLocation = mutableMapOf<SectionLocation, Int>()
        // Accuracy is the number of levels that the location should be trimmed to.
        // For example, if the accuracy is 2, the location `1.2.3.4` will be trimmed to `1.2`.
        // The last numbering symbol is reserved for the element's own ID.
        val accuracy = format.accuracy - 1

        iterator.on<T> { node ->
            if (!filter(node)) return@on

            // A location has to be already stored for the given node (see LocationAwarenessHook)
            val location = context.attributes.locations[node] ?: return@on

            // The location of the element is trimmed to the desired accuracy.
            val trimmedLocation =
                SectionLocation(
                    when {
                        // If the location has more nested levels than the accuracy, it is trimmed.
                        // e.g. Location: `2.1.0.1`, Accuracy: 2 -> Result: `2.1`
                        location.levels.size > accuracy -> location.levels.take(accuracy)
                        // If the location has less nested levels than the accuracy, it is padded with zeroes.
                        // e.g. Location: `2.1`, Accuracy: 4 -> Result: `2.1.0.0`
                        location.levels.size < accuracy -> location.levels + Array(accuracy - location.levels.size) { 0 }
                        // Location levels and accuracy match.
                        else -> location.levels
                    },
                )

            // The number of elements encountered at the trimmed location is updated.
            val count = (countAtLocation[trimmedLocation] ?: 0) + 1
            countAtLocation[trimmedLocation] = count

            // Given the trimmed location of the element,
            // its index is appended to it in order to form the final label of the element.
            val label = location.copy(levels = trimmedLocation.levels + count)

            // The stringified label is stored.
            context.attributes.positionalLabels[node] = format.format(label)
        }
    }
}
