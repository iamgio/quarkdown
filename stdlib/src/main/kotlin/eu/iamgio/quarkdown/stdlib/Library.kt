package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.ListValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Library` stdlib module exporter.
 * This module handles loaded libraries and their functions.
 */
val Library =
    setOf(
        ::libraryExists,
        ::functionExists,
        ::libraryFunctions,
    )

/**
 * @param context context to search in
 * @param name name of the library, case-insensitive
 * @return library with the given name, if it exists

 */
private fun findLibrary(
    context: Context,
    name: String,
): Library? = context.libraries.find { it.name.equals(name, ignoreCase = true) }

/**
 * @param name name of the library, case-insensitive
 * @return whether a library with the given name is registered in [context]
 */
@FunctionName("libexists")
fun libraryExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(findLibrary(context, name) != null)

/**
 * @param name name of the function, case-insensitive
 * @return whether a function with the given name is registered in [context]
 */
@FunctionName("functionexists")
fun functionExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(context.getFunctionByName(name) != null)

/**
 * @param libraryName name of the library, case-insensitive
 * @return list of functions exposed by the library
 */
@FunctionName("libfunctions")
fun libraryFunctions(
    @Injected context: Context,
    libraryName: String,
): ListValue<StringValue> =
    (findLibrary(context, libraryName)?.functions?.map { it.name.wrappedAsValue() } ?: emptyList())
        .wrappedAsValue()
