@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: multiple `@QFunction`s in the same module. Verifies that `moduleOf` receives every
 * function reference in source order and that each wrapper is emitted.
 */

@QFunction
fun alpha() = VoidValue

@QFunction
fun bravo(text: String) = VoidValue

@QFunction
fun charlie(
    a: Int,
    b: Int,
) = VoidValue
