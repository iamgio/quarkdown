package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.BooleanValue

/**
 * `Library` stdlib module exporter.
 * This module handles loaded libraries and their functions.
 */
val Library =
    setOf(
        ::libraryExists,
        ::functionExists,
    )

/**
 * @param name name of the library, case-insensitive
 * @return whether a library with the given name is registered in [context]
 */
@FunctionName("libraryexists")
fun libraryExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(context.libraries.any { it.name.equals(name, ignoreCase = true) })

/**
 * @param name name of the function, case-insensitive
 * @return whether a function with the given name is registered in [context]
 */
@FunctionName("functionexists")
fun functionExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(context.getFunctionByName(name) != null)
