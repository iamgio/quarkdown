package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.util.replace

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
 * @return the evaluation of [body] if [condition] is `true`, otherwise nothing
 */
@Name("if")
fun `if`(
    @Injected context: Context,
    condition: Boolean,
    body: String,
): OutputValue<*> =
    when (condition) {
        true -> ValueFactory.expression(body, context)?.eval() as OutputValue<*>
        false -> VoidValue
    }

/**
 * @param condition whether the content should be added to the document
 * @return [body] if [condition] is `false`, otherwise nothing
 */
@Name("ifnot")
fun ifNot(
    @Injected context: Context,
    condition: Boolean,
    body: String,
): OutputValue<*> = `if`(context, !condition, body)

/**
 * Repeats content for each element of an iterable collection.
 * The current element can be accessed via the `<<name>>` placeholder, which defaults to `<<1>>`.
 * @param iterable collection to iterate
 * @param name placeholder to access the current element (wrapped in double angle brackets)
 * @param body content, output of each iteration
 * @return a collection that contains the output of each iteration
 */
@Name("foreach")
fun forEach(
    @Injected context: Context,
    iterable: Iterable<Value<*>>,
    name: String = "1",
    body: String,
): IterableValue<OutputValue<*>> {
    val values =
        iterable.map {
            val content = body.replace("<<$name>>", it.unwrappedValue.toString())
            ValueFactory.expression(content, context)?.eval() as OutputValue<*>
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
 * @param body content of the function
 */
fun function(
    @Injected context: MutableContext,
    name: String,
    body: String,
): VoidValue {
    // Matches '<<1>>', '<<2>>', etc.
    // These are the spots that will be replaced with actual arguments.
    val replacementsRegex = "(?<!\\\\)<<(\\d+)>>".toRegex()
    val matches = replacementsRegex.findAll(body)

    // The amount of parameters is the highest replacement number found (the number is captured in groupValues[1]).
    // e.g. if the body contains <<1>> and <<2>>, the function will have 2 parameters.
    val paramCount =
        matches.sortedByDescending { it.groupValues[1].toInt() }.firstOrNull()
            ?.groupValues?.get(1)?.toInt() ?: 0

    // Function parameters.
    val params =
        (0 until paramCount).map {
            FunctionParameter(name = "param$it", type = String::class, index = it)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, params) {
            val content = StringBuilder(body)

            // Upon invocation, replaces the placeholders with actual arguments.
            // Argument 0 replaces <<1>> and so on.
            this.links.forEach { (parameter, argument) ->
                val replacement = "<<${parameter.index + 1}>>"
                val value = argument.value.unwrappedValue.toString() // Only string replacements are supported.
                content.replace(replacement, value)
            }

            // The final content is tokenized, evaluated and returned.
            ValueFactory.dynamic(content.toString(), context)
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

    return function(context, name, value)
}
