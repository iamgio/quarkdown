package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.InlineMarkdownContent
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.quarkdown.inline.TextTransform
import eu.iamgio.quarkdown.ast.quarkdown.inline.TextTransformData
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Text` stdlib module exporter.
 * This module handles text formatting and manipulation.
 */
val Text: Module =
    setOf(
        ::text,
        ::code,
        ::loremIpsum,
    )

/**
 * Creates an inline text node with specified formatting and transformation.
 * @param size font size, or default if not specified
 * @param weight font weight, or default if not specified
 * @param style font style, or default if not specified
 * @param decoration text decoration, or default if not specified
 * @param case text case, or default if not specified
 * @param variant font variant, or default if not specified
 * @param color text color, or default if not specified
 * @param content inline content to transform
 */
fun text(
    size: TextTransformData.Size? = null,
    weight: TextTransformData.Weight? = null,
    style: TextTransformData.Style? = null,
    decoration: TextTransformData.Decoration? = null,
    case: TextTransformData.Case? = null,
    variant: TextTransformData.Variant? = null,
    color: Color? = null,
    content: InlineMarkdownContent,
): NodeValue =
    TextTransform(
        TextTransformData(size, weight, style, decoration, case, variant, color),
        content.children,
    ).wrappedAsValue()

/**
 * Creates a code block. Contrary to its standard Markdown implementation with backtick/tilde fences,
 * this function accepts Markdown content as its body, hence it can be used - for example -
 * in combination with [read] to load code from file.
 * @param language optional language of the code
 * @param showLineNumbers whether to show line numbers
 * @param focusedLines range of lines to focus on. No lines are focused if unset. Supports open ranges.
 * Note: HTML rendering requires [showLineNumbers] to be enabled.
 * @param body code content
 */
fun code(
    @Injected context: MutableContext,
    @Name("lang") language: String? = null,
    @Name("linenumbers") showLineNumbers: Boolean = true,
    @Name("focus") focusedLines: Range? = null,
    body: MarkdownContent,
): NodeValue {
    context.attributes.hasCode = true // Allows code highlighting.
    return Code(
        body.children.toPlainText(),
        language,
        showLineNumbers,
        focusedLines,
    ).wrappedAsValue()
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
