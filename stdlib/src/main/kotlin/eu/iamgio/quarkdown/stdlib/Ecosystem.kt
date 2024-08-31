package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.error.FunctionRuntimeException
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.ValueFactory

/**
 * `Ecosystem` stdlib module exporter.
 * This module handles interaction between Quarkdown sources.
 */
val Ecosystem: Module =
    setOf(
        ::include,
    )

/**
 * Reads a Quarkdown file and includes its parsed content in the current document.
 * The context of the main file is shared to the sub-file, allowing for sharing of variables, functions and other declarations.
 * @param path path (relative or absolute) to the file to include, with extension.
 * @return the parsed content of the file.
 * @throws FunctionRuntimeException if the loaded Quarkdown source cannot be evaluated or if it cannot be evaluated into a suitable output value
 */
fun include(
    @Injected context: Context,
    path: String,
): OutputValue<*> {
    // Read file content
    val file = file(context, path)
    val raw = "\n" + file.readText()
    // The initial line break is a workaround for an issue that may cause blocks at the beginning of the file
    // to be not recognized as blocks - which happens in transitive inclusions (aka a nested .include called from an included file).

    // Evaluate the Quarkdown source.
    // This automatically converts the source into a value (e.g. a node, a string, a number, etc.)
    // and fills the current context with new declarations (e.g. variables, functions, link definitions, etc.)
    return ValueFactory.eval(raw, context)
}
