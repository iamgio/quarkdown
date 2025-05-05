package com.quarkdown.stdlib

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.UnorderedCollectionValue
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Library` stdlib module exporter.
 * This module handles loaded libraries and their functions.
 */
val Library: Module =
    setOf(
        ::libraryExists,
        ::functionExists,
        ::libraries,
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
@Name("libexists")
fun libraryExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(findLibrary(context, name) != null)

/**
 * @param name name of the function, case-insensitive
 * @return whether a function with the given name is registered in [context]
 */
@Name("functionexists")
fun functionExists(
    @Injected context: Context,
    name: String,
) = BooleanValue(context.getFunctionByName(name) != null)

/**
 * @return an unordered collection of the loaded libraries' names
 */
fun libraries(
    @Injected context: Context,
) = UnorderedCollectionValue(
    context.libraries
        .asSequence()
        .map { it.name.wrappedAsValue() }
        .toSet(),
)

/**
 * @param libraryName name of the library, case-insensitive
 * @return unordered set of functions exposed by the library
 */
@Name("libfunctions")
fun libraryFunctions(
    @Injected context: Context,
    libraryName: String,
) = UnorderedCollectionValue(
    findLibrary(context, libraryName)
        ?.functions
        ?.asSequence()
        ?.map { it.name.wrappedAsValue() }
        ?.toSet()
        ?: emptySet(),
)
