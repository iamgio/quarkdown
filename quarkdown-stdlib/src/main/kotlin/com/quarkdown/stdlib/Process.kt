@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Retrieves the value of an environment variable.
 *
 * Example:
 * ```
 * .env {HOME}
 * ```
 *
 * @param name name of the environment variable to look up
 * @return the value of the environment variable as a string, or `none` if not set
 * @permission [Permission.ProcessAccess] to access environment variables
 * @wiki environment
 */
@QFunction
fun env(
    @Injected context: Context,
    name: String,
): OutputValue<*> {
    context.requirePermission(
        Permission.ProcessAccess,
        message = "Cannot read environment variable '$name'",
    )
    return System
        .getenv(name)
        ?.let(::StringValue)
        ?: NoneValue
}
