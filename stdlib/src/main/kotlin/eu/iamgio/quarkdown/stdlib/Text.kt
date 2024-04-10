package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * `Text` stdlib module exporter.
 */
val Text =
    setOf(
        ::test,
        ::greet,
        ::bold,
    )

fun test(x: Int = 0) = StringValue("Test $x from function!!!")

fun greet(name: String) = StringValue("Hello $name")

fun bold(body: MarkdownContent) =
    NodeValue(
        Strong(body.children),
    )
