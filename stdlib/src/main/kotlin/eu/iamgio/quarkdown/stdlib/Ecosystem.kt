package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.error.FunctionRuntimeException
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import java.io.File

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
    val raw = File(path).readText()
    val result =
        ValueFactory.expression(raw, context)?.eval()
            ?: throw FunctionRuntimeException("Cannot include sub-file $path: the Quarkdown source could not be evaluated")

    return result as? OutputValue<*>
        ?: throw FunctionRuntimeException(
            "Cannot include sub-file $path: the evaluation of the Quarkdown source is not a suitable output value " +
                "(${result::class.simpleName} found)",
        )
}
