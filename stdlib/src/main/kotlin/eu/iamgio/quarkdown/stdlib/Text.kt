package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Text` stdlib module exporter.
 * This module handles text formatting and manipulation.
 */
val Text =
    setOf(
        ::code,
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
