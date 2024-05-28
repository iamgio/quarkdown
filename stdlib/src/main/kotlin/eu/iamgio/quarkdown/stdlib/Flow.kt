package eu.iamgio.quarkdown.stdlib

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
    condition: Boolean,
    body: Lambda,
): OutputValue<*> =
    when (condition) {
        true -> body.invokeDynamic()
        false -> VoidValue
    }

/**
 * @param condition whether the content should be added to the document
 * @param body content to append if [condition] is not verified. Accepts 0 parameters.
 * @return [body] if [condition] is `false`, otherwise nothing
 */
@Name("ifnot")
fun ifNot(
    condition: Boolean,
    body: Lambda,
): OutputValue<*> = `if`(!condition, body)

/**
 * Repeats content for each element of an iterable collection.
 * The current element can be accessed via the lambda argument.
 * @param iterable collection to iterate
 * @param body content, output of each iteration. Accepts 1 parameter (the current element).
 * @return a collection that contains the output of each iteration
 */
@Name("foreach")
fun forEach(
    iterable: Iterable<Value<*>>,
    body: Lambda,
): IterableValue<OutputValue<*>> {
    val values =
        iterable.map {
            body.invokeDynamic(it)
        }

    return GeneralCollectionValue(values)
}

/**
 * Custom functions (via [function]) and variables (via [variable]) are saved in a [Library]
 * whose name begins by this string.
 */
private const val CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX = "__func__"

/**
 * Defines a custom function that can be called later in the document.
 * The amount of parameters (thus of expected arguments) is determined by the amount of **explicit** lambda parameters.
 * Arguments can be accessed as a function call with their names.
 * The return type of the function is dynamic, hence it can be used as an input of various types for other function calls.
 *
 * Example:
 * ```
 * .function {greet}
 *     from to:
 *     **Hello .to** from .from
 * ```
 *
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
            val result = body.invokeDynamic(*args)
            // The final content is evaluated and returned as a dynamic, hence it can be used as any type.
            ValueFactory.dynamic(result.unwrappedValue.toString(), context)
        }

    // The function is registered and ready to be called.
    context.libraries += Library(CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + name, setOf(function))

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
    context.libraries.removeIf { it.name == CUSTOM_FUNCTION_LIBRARY_NAME_PREFIX + name }

    return function(
        context,
        name,
        Lambda(context, explicitParameters = emptyList()) {
            value
        },
    )
}
