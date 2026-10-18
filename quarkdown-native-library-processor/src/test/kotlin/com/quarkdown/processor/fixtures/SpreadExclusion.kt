@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.annotation.Spread

/*
 * Fixture: `@Spread(exclude = [...])` leaves the listed components out of the wrapper. Covers:
 * - an excluded component whose exported name would clash with a plain sibling (`alignment`),
 * - the excluded component falling back to its constructor default in the reconstruction,
 * - the excluded component's `@param` tag being dropped from the wrapper KDoc,
 * - non-excluded components still being expanded as usual.
 */

/**
 * @param alignment the excluded component; its tag must not reach the wrapper
 * @param backgroundColor the fill color
 */
data class Appearance(
    val alignment: String = "start",
    @Name("background") val backgroundColor: String = "white",
)

/**
 * @param alignment the function's own alignment, which wins over the spread's
 */
@QFunction
fun withExcludedComponent(
    alignment: Int,
    @Spread(exclude = ["alignment"]) appearance: Appearance = Appearance(),
) = VoidValue
