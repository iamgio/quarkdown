@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.annotation.Spread

/*
 * Fixture: verifies the wrapper carries the source function's KDoc with parameter names rewritten
 * to their exported form, and that a `@Spread` parameter has the data class's own `@param` tags
 * injected in its place (also rewritten).
 */

/**
 * Documentation for the [Box] data class.
 * The class-level description here must NOT bleed into the wrapper KDoc.
 *
 * @param innerColor the fill color. See [innerColor] for the reference form
 * @param innerWidth the width; defaults handled by the data class
 */
data class Box(
    @Name("color") val innerColor: String,
    @LikelyNamed val innerWidth: Double = 1.0,
)

/**
 * A documented function whose KDoc must survive substitution.
 *
 * Reference to [label] should be rewritten to the exported name.
 *
 * @param label the label to attach; see [label] again for the link form
 * @param count how many boxes to render
 * @param box the outer spread parameter must be filtered out from the wrapper KDoc
 * @return the produced value
 * @wiki some-page
 */
@QFunction
fun documentedFunction(
    @Name("caption") label: String,
    count: Int = 1,
    @Spread box: Box = Box("red"),
) = VoidValue
