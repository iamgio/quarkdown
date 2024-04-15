package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Text` stdlib module exporter.
 * This module handles text formatting and manipulation.
 */
val Text =
    setOf(
        ::code,
        ::loremIpsum,
    )

/**
 * Creates a code block. Contrary to its standard Markdown implementation with backtick/tilde fences,
 * this function accepts Markdown content as its body, hence it can be used - for example -
 * in combination with [fileContent] to load code from file.
 * @param language optional language of the code
 * @param body code content
 */
fun code(
    language: String? = null,
    body: MarkdownContent,
) = NodeValue(
    Code(body.children.toPlainText(), language),
)

/**
 * @return a fixed Lorem Ipsum text.
 */
@FunctionName("loremipsum")
fun loremIpsum() =
    StringValue(
        object {}::class.java.getResourceAsStream("/text/lorem-ipsum.txt")!!
            .reader().readText(),
    )
