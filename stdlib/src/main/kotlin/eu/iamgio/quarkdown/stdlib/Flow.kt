package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.data.Lambda

/**
 * `Flow` stdlib module exporter.
 * This module handles the control flow and other statements.
 */
val Flow: Module =
    setOf(
        ::`if`,
        ::ifNot,
        ::forEach,
        ::function,
        ::variable,
    )

/**
 * @param condition whether the content should be added to the document
 * @param body content to append if [condition] is verified. Accepts 0 parameters.
 * @return the evaluation of [body] if [condition] is `true`, otherwise nothing
 */
@Name("if")
fun `if`(
    @Injected context: Context,
    condition: Boolean,
    body: Lambda,
): OutputValue<*> =
    when (condition) {
        true -> body.invokeDynamic(context)
        false -> VoidValue
    }

/**
 * @param condition whether the content should be added to the document
 * @param body content to append if [condition] is not verified. Accepts 0 parameters.
 * @return [body] if [condition] is `false`, otherwise nothing
 */
@Name("ifnot")
fun ifNot(
    @Injected context: Context,
    condition: Boolean,
    body: Lambda,
): OutputValue<*> = `if`(context, !condition, body)

/**
 * Repeats content for each element of an iterable collection.
 * The current element can be accessed via the `<<name>>` placeholder, which defaults to `<<1>>`.
 * @param iterable collection to iterate
 * @param body content, output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 */
@Name("foreach")
fun forEach(
    @Injected context: Context,
    iterable: Iterable<Value<*>>,
    body: Lambda,
): IterableValue<OutputValue<*>> {
    val values =
        iterable.map {
            body.invokeDynamic(context, it)
        }

    return GeneralCollectionValue(values)
}

/**
 * Defines a custom function that can be called later in the document.
 * The function can have placeholders that will be replaced with actual arguments upon invocation,
 * defined as `<<1>>`, `<<2>>`, and so on, always starting from 1.
 *
 * Unwanted results may be produced if:
 * - `<<0>>` is referenced
 * - References are not in a continuous ascending sequence (e.g. `<<3>>` is referenced but `<<2>>` is not).
 *
 * The amount of parameters (thus of expected arguments) is determined by the highest number among the placeholders.
 * Upon invocation, the placeholders are replaced with the string representation of the actual arguments.
 * The return type of the function is dynamic, hence it can be used as an input of various types for other function calls.
 * @param name name of the function
 * @param body content of the function. Function parameters must be **explicit** lambda parameters
 */
fun function(
    @Injected context: MutableContext,
    name: String,
    body: Lambda,
): VoidValue {
    // Function parameters.
    val parameters =
        body.explicitParameters.mapIndexed { index, parameter ->
            FunctionParameter(name = parameter, type = String::class, index)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, parameters) {
            val args = this.links.values.map { it.value }.toTypedArray()
            val result = body.invokeDynamic(context, *args)
            // The final content is evaluated and returned as a dynamic, hence it can be used as any type.
            ValueFactory.dynamic(result.unwrappedValue.toString(), context)
        }

    // The function is registered and ready to be called.
    context.libraries += Library(name, setOf(function))

    return VoidValue
}

/**
 * Defines a new variable or overwrites an existing one.
 * Variables can be referenced just like functions, via `.variablename`.
 * @param name name of the variable
 * @param value value to assign
 */
@Name("var")
fun variable(
    @Injected context: MutableContext,
    name: String,
    value: String,
): VoidValue {
    // Deletes previous values.
    context.libraries.removeIf { it.name == name }

    return function(
        context,
        name,
        Lambda(explicitParameters = emptyList()) {
            value
        },
    )
}
