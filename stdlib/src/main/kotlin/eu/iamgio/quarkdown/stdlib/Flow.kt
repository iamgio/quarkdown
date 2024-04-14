package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.VoidValue

/**
 * `Flow` stdlib module exporter.
 * This module handles the control flow and conditional statements.
 */
val Flow =
    setOf(
        ::`if`,
        ::ifNot,
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
