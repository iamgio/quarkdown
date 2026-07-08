@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: a single `@QFunction` in a `@file:QModule`. Exercises the baseline
 * wrapper shape (`object SimpleLogger { val Module = moduleOf(this::log); fun log(...) = ... }`).
 */

@QFunction
fun logSimple(message: String) = VoidValue
