package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.factory.ValueFactory
import java.io.Reader

/**
 * `Ecosystem` stdlib module exporter.
 * This module handles interaction between Quarkdown sources.
 */
val Ecosystem: Module =
    setOf(
        ::include,
    )

/**
 * Includes the parsed content of the given raw Quarkdown [code], read by [reader], in the current document.
 * The context of the main file is shared, allowing for sharing of variables, functions and other declarations.
 * @param context main context to share
 * @param reader reader of the raw Quarkdown source to include
 * @return the content of the file as a node
 */
fun includeResource(
    context: Context,
    reader: Reader,
): NodeValue {
    val code = reader.readText()

    // Evaluate the Quarkdown source.
    // This automatically converts the source into a value (e.g. a node, a string, a number, etc.)
    // and fills the current context with new declarations (e.g. variables, functions, link definitions, etc.)
    return ValueFactory.blockMarkdown(code, context).asNodeValue()
}

/**
 * This function has two behaviors:
 * - Reads a Quarkdown file and includes its parsed content in the current document.
 *   Loadable libraries are fetched from the library folder (`--libs` option).
 * - Loads a present but unloaded library into the current context.
 * The context of the main file is shared to the sub-file and vice-versa, allowing for sharing of variables, functions and other declarations.
 * @param path either a path (relative or absolute with extension) to the file to include, or the name of a loadable library
 * @return the content of the file as a node if a file is included, or nothing if a library is loaded
 * @throws IllegalArgumentException if the loaded Quarkdown source cannot be evaluated or if it cannot be evaluated into a suitable output value
 */
fun include(
    @Injected context: MutableContext,
    path: String,
): OutputValue<*> {
    // Load library by name if it exists.
    context.loadLibrary(path)?.let { return VoidValue }

    // Include file content.
    val file = file(context, path)
    return includeResource(context, file.bufferedReader())
}
