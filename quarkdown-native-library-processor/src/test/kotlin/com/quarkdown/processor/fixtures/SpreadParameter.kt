@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.annotation.Spread

/*
 * Fixture: `@Spread` expands a data class into wrapper parameters. Covers:
 * - `@Name` rewriting on spread components,
 * - a component-level default value,
 * - an `@LikelyNamed` annotation on a component that must propagate to the wrapper,
 * - a plain sibling parameter that must interleave in declaration order (plain before spread),
 * - a component default that references a renamed sibling
 *   (`color = foregroundColor` -> `color = foreground` in the wrapper).
 */

data class Style(
    @Name("foreground") val foregroundColor: String,
    @Name("background") val backgroundColor: String = "white",
    @LikelyNamed val opacity: Double = 1.0,
    val color: String = foregroundColor,
)

@QFunction
fun withSpreadStyle(
    text: String,
    @Spread style: Style,
) = VoidValue
