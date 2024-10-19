package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.attributes.AstAttributes
import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.attributes.SectionLocation
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext

/**
 * Hook that stores the location of each [LocationTrackableNode] in the document.
 * @see LocationTrackableNode
 * @see AstAttributes.locations
 */
class SectionLocationHook(private val context: MutableContext) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        // Stores the current section location.
        // The key is the depth of the last heading found;
        // the value is the index of the section.
        val location = mutableMapOf<Int, Int>()

        // Note: the two following hooks are executed in parallel.

        // When a heading is found, the current location is updated.
        // Example:
        // current location: []
        // # A       => current location: [0]
        // ## A.A    => current location: [0, 0]
        // # B       => current location: [1]
        // ## B.A    => current location: [1, 0]
        // ### B.A.A => current location: [1, 0, 0]
        // ### B.A.B => current location: [1, 0, 1]
        // # C       => current location: [2]
        // ### C.A.A => current location: [2, 0, 0]
        iterator.on<Heading> { heading ->
            location[heading.depth] = location[heading.depth]?.plus(1) ?: 0
            location.entries.removeIf { it.key > heading.depth }

            // Gap filler: e.g. if an H1 is followed by an H3, an H2 is automatically added.
            var firstFound = false // The gaps are filled only in-between, never at the beginning.
            for (i in 0 until heading.depth) {
                when {
                    location[i] != null -> firstFound = true
                    firstFound -> location[i] = 0
                }
            }
        }

        // The current location, loaded by the previous hook, is associated with each node that requests its location to be tracked.
        iterator.on<LocationTrackableNode> { trackable ->
            val locationData =
                location.asSequence()
                    .sortedBy { it.key }
                    .map { it.value }

            // Registration of the location.
            context.attributes.locations[trackable] = SectionLocation(locationData.toList())
        }
    }
}
