package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.reflect.toType
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.data.Lambda1
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Text` stdlib module exporter.
 * This module handles text formatting and manipulation.
 */
val Text: Module =
    setOf(
        ::code,
        ::loremIpsum,
        ::greet,
    )

fun greet(
    @Injected context: Context,
    x: Lambda1,
): StringValue = x("Gio".wrappedAsValue()).toType<String, StringValue>(context)

/**
 * Creates a code block. Contrary to its standard Markdown implementation with backtick/tilde fences,
 * this function accepts Markdown content as its body, hence it can be used - for example -
 * in combination with [fileContent] to load code from file.
 * @param language optional language of the code
 * @param body code content
 */
fun code(
    @Injected context: MutableContext,
    language: String? = null,
    body: MarkdownContent,
): NodeValue {
    context.hasCode = true // Allows code highlighting.
    return Code(body.children.toPlainText(), language).wrappedAsValue()
}

/**
 * @return a fixed Lorem Ipsum text.
 */
@Name("loremipsum")
fun loremIpsum() =
    object {}::class.java.getResourceAsStream("/text/lorem-ipsum.txt")!!
        .reader()
        .readText()
        .wrappedAsValue()
