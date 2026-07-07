@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: `@Name` on the function renames the exported wrapper.
 * The source-level name (`logInternal`) survives on the FQN delegation call;
 * the exported wrapper and the `moduleOf` entry both use `renamedLog`.
 */

@QFunction
@Name("renamedLog")
fun logInternal(message: String) = VoidValue
