@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: `@Name` on a parameter renames the wrapper parameter but leaves the delegation
 * call using the source name (`text = renamedText`).
 */

@QFunction
fun logWithNamedParam(
    @Name("renamedText") text: String,
) = VoidValue
