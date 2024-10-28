package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.attributes.AstAttributes
import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.attributes.SectionLocation
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.numbering.NumberingFormat

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
 * @see AstAttributes.labels
 * @see AstAttributes.locations
 * @see LocationTrackableNode
 */
class LocationAwareLabelStorerHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        updateLabels<ImageFigure>(context.documentInfo.numberingFormatOrDefault, iterator) // TODO specific figure format
    }

    /**
     * Updates labels of elements of type [T] based on their location in the document and the given numbering format.
     * @param format numbering format used to generate the labels for this type of element
     * @param iterator iterator to attach the hook to
     * @param T type of elements to update the labels of
     */
    private inline fun <reified T : LocationTrackableNode> updateLabels(
        format: NumberingFormat?,
        iterator: ObservableAstIterator,
    ) {
        if (format == null) return

        // Stores the number of elements encountered at each location.
        val countAtLocation = mutableMapOf<SectionLocation, Int>()
        // Accuracy is the number of levels that the location should be trimmed to.
        // For example, if the accuracy is 2, the location `1.2.3.4` will be trimmed to `1.2`.
        // The last numbering symbol is reserved for the element's own ID.
        val accuracy = format.accuracy - 1

        iterator.on<T> { node ->
            // A location has to be already stored for the given node (see LocationAwarenessHook)
            val location = context.attributes.locations[node] ?: return@on

            // The location of the element is trimmed to the desired accuracy.
            val trimmedLocation = location.copy(levels = location.levels.take(accuracy))

            // The number of elements encountered at the trimmed location is updated.
            val count = countAtLocation[trimmedLocation] ?: 0
            countAtLocation[trimmedLocation] = count + 1

            // Given the trimmed location of the element,
            // its index is appended to it in order to form the final label of the element.
            val label = location.copy(levels = trimmedLocation.levels + count)

            // The stringified label is stored.
            context.attributes.labels[node] = format.format(label)
        }
    }
}
