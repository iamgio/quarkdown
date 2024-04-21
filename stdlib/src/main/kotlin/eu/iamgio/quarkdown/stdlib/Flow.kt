package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.SimpleFunction
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.*
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
 * Repeats content for each element of an iterable collection.
 * The current element can be accessed via the `{{<name>}}` placeholder, which defaults at `{{1}}`.
 * @param iterable collection to iterate
 * @param name placeholder to access the current element
 * @param body content, output of each iteration
 * @return a new node that contains [body] repeated for each element
 */
@FunctionName("foreach")
fun forEach(
    @Injected context: Context,
    iterable: Iterable<Value<*>>,
    name: String = "1",
    body: String,
): NodeValue {
    val nodes = mutableListOf<Node>()
    iterable.forEach {
        val content = body.replace("{{$name}}", it.unwrappedValue.toString())
        nodes.addAll(ValueFactory.markdown(content, context).unwrappedValue.children)
    }
    return NodeValue(MarkdownContent(nodes))
}

/**
 * Defines a custom function that can be called later in the document.
 * The function can have placeholders that will be replaced with actual arguments upon invocation,
 * defined as `{{0}}`, `{{1}}`, etc.
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
    // Matches '{{1}}', '{{2}}', etc.
    // These are the spots that will be replaced with actual arguments.
    val replacementsRegex = "(?<!\\\\)\\{\\{(\\d+)}}".toRegex()
    val matches = replacementsRegex.findAll(body)

    // The amount of parameters is the highest replacement number found (the number is captured in groupValues[1]).
    // e.g. if the body contains {{1}} and {{2}}, the function will have 2 parameters.
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
            // Argument 0 replaces {{1}} and so on.
            this.links.forEach { (parameter, argument) ->
                val replacement = "{{${parameter.index + 1}}}"
                val value = argument.value.unwrappedValue.toString() // Only string replacements are supported.
                content.replace(replacement, value)
            }

            // The final content is parsed as Markdown and returned.
            // ValueFactory.markdown(content.toString(), context).asNodeValue()
            DynamicValue(content.toString())
        }

    // The function is registered and ready to be called.
    context.libraries += Library(name, setOf(function))

    return VoidValue
}
