@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: default-value forwarding. Covers a plain literal default, a boolean default, and a
 * default that references an earlier `@Name`-renamed sibling parameter (the identifier `first`
 * in the source must be rewritten to `primary` in the wrapper).
 */

@QFunction
fun withSimpleDefaults(
    message: String,
    prefix: String = "[quarkdown]",
    repeat: Int = 1,
) = VoidValue

@QFunction
fun withCrossReferenceDefault(
    @Name("primary") first: String,
    second: String = first,
) = VoidValue
