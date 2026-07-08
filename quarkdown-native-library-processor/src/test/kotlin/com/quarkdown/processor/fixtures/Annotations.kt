@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: exercises annotation propagation. Every source annotation should reach the wrapper
 * except @Name (handled at the symbol level by renaming) and @QFunction (the processor's own
 * marker). A parameter combines @Name with @LikelyNamed to verify partial filtering: the wrapper
 * should show @LikelyNamed on the renamed parameter but no @Name.
 */

@QFunction
@LikelyChained
fun withAnnotations(
    @Injected context: Context,
    @LikelyNamed text: String,
    @Name("count") @LikelyNamed size: Int = 1,
) = VoidValue.also {
    // Reference `context` so it isn't a warning, even though we don't use it.
    context.hashCode()
}
