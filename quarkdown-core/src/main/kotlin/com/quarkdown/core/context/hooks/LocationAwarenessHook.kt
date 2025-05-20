package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.SectionLocation
import com.quarkdown.core.ast.attributes.location.setLocation
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext

/**
 * Hook that stores the location of each [LocationTrackableNode] in the document.
 * @see LocationTrackableNode
 * @see com.quarkdown.core.ast.attributes.location
 */
class LocationAwarenessHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        // Stores the current section location.
        // The key is the depth of the last heading found;
        // the value is the index of the section.
        val location = mutableMapOf<Int, Int>()

        // Note: the two following hooks are executed in 'parallel'.

        // When a heading is found, the current location is updated.
        // Example:
        // current location: []
        // # A       => current location: [1]
        // ## A.A    => current location: [1, 1]
        // # B       => current location: [2]
        // ## B.A    => current location: [2, 1]
        // ### B.A.A => current location: [2, 1, 1]
        // ### B.A.B => current location: [2, 1, 2]
        // # C       => current location: [3]
        // ### C.0.A => current location: [3, 0, 1]
        iterator.on<Heading> { heading ->
            if (!heading.canTrackLocation) return@on // 'Decorative' headings are not assigned a location and are not counted.

            location[heading.depth] = (location[heading.depth] ?: 0) + 1
            location.entries.removeIf { it.key > heading.depth }

            // Gap filler: e.g. if an H1 is followed by an H3, a mock H2 with value '0' is automatically added.
            for (i in 1 until heading.depth) {
                if (location[i] == null) {
                    location[i] = 0
                }
            }
        }

        // The current location, loaded by the previous hook, is associated with each node that requests its location to be tracked.
        iterator.on<LocationTrackableNode> { trackable ->
            if (!trackable.canTrackLocation) return@on

            val locationData =
                location
                    .asSequence()
                    .sortedBy { it.key }
                    .map { it.value }

            // Registration of the location.
            trackable.setLocation(context, SectionLocation(locationData.toList()))
        }
    }
}
