package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.util.replace

/**
 * `Flow` stdlib module exporter.
 * This module handles the control flow and other statements.
 */
val Flow =
    setOf(
        ::`if`,
        ::ifNot,
        ::function,
    )

/**
 * @param condition whether the content should be added to the document
 * @return [body] if [condition] is `true`, otherwise nothing
 */
@FunctionName("if")
fun `if`(
    condition: Boolean,
    body: MarkdownContent,
): OutputValue<*> =
    when (condition) {
        true -> NodeValue(body)
        false -> VoidValue
    }

/**
 * @param condition whether the content should be added to the document
 * @return [body] if [condition] is `false`, otherwise nothing
 */
@FunctionName("ifnot")
fun ifNot(
    condition: Boolean,
    body: MarkdownContent,
): OutputValue<*> = `if`(!condition, body)

/**
 * Defines a custom function that can be called later in the document.
 * The function can have placeholders that will be replaced with actual arguments upon invocation,
 * defined as `{{0}}`, `{{1}}`, etc.
 * The amount of parameters (thus of expected arguments) is determined by the highest number among the placeholders.
 * Upon invocation, the placeholders are replaced with the string representation of the actual arguments.
 * @param name name of the function
 * @param body content of the function
 */
fun function(
    @Injected context: MutableContext,
    name: String,
    body: String,
): VoidValue {
    // Matches '{{0}}', '{{1}}', etc.
    // These are the spots that will be replaced with actual arguments.
    val replacementsRegex = "(?<!\\\\)\\{\\{(\\d+)}}".toRegex()
    val matches = replacementsRegex.findAll(body)

    // The amount of parameters is the highest replacement number found (the number is captured in groupValues[1]).
    val paramCount =
        matches.sortedByDescending { it.groupValues[1].toInt() }.firstOrNull()
            ?.groupValues?.get(1)?.toInt() ?: 0

    // Function parameters.
    val params =
        (0..paramCount).map {
            FunctionParameter(name = "param$it", type = String::class, index = it)
        }

    // The custom function itself.
    val function =
        SimpleFunction(name, params) {
            val content = StringBuilder(body)

            // Upon invocation, replaces the placeholders with actual arguments.
            this.links.forEach { (parameter, argument) ->
                val replacement = "{{${parameter.index}}}"
                val value = argument.value.unwrappedValue.toString() // Only string replacements are supported.
                content.replace(replacement, value)
            }

            // The final content is parsed as Markdown and returned.
            ValueFactory.markdown(content.toString(), context).asNodeValue()
        }

    // The function is registered and ready to be called.
    context.libraries += Library(name, setOf(function))

    return VoidValue
}
