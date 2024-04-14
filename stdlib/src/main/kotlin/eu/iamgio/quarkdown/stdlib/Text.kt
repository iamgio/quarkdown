package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Text` stdlib module exporter.
 */
val Text =
    setOf(
        ::test,
        ::greet,
        ::bold,
        ::code,
        ::align,
        ::center,
        ::clip,
    )

fun test(x: Int = 0) = StringValue("Test $x from function!!!")

fun greet(name: String) = StringValue("Hello $name")

fun bold(body: MarkdownContent) =
    NodeValue(
        Strong(body.children),
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
 * Aligns content within its parent.
 * @param alignment content alignment anchor
 * @param body content to center
 * @return the new aligned block
 */
fun align(
    alignment: Aligned.Alignment,
    body: MarkdownContent,
) = NodeValue(
    Aligned(alignment, body.children),
)

/**
 * Centers content within its parent.
 * @param body content to center
 * @return the new aligned block
 * @see align
 */
fun center(body: MarkdownContent) = align(Aligned.Alignment.CENTER, body)

/**
 * Applies a clipping path to its content.
 * @param clip clip type to apply
 * @return the new clipped block
 */
fun clip(
    clip: Clipped.Clip,
    body: MarkdownContent,
) = NodeValue(
    Clipped(clip, body.children),
)
