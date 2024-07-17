package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.InlineMarkdownContent
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.quarkdown.TextTransform
import eu.iamgio.quarkdown.ast.quarkdown.TextTransformData
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
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
 * @param content inline content to transform
 * @param size font size, or default if not specified
 * @param weight font weight, or default if not specified
 * @param style font style, or default if not specified
 * @param decoration text decoration, or default if not specified
 * @param case text case, or default if not specified
 * @param variant font variant, or default if not specified
 * @param color text color, or default if not specified
 */
fun text(
    content: InlineMarkdownContent,
    size: TextTransformData.Size? = null,
    weight: TextTransformData.Weight? = null,
    style: TextTransformData.Style? = null,
    decoration: TextTransformData.Decoration? = null,
    case: TextTransformData.Case? = null,
    variant: TextTransformData.Variant? = null,
    color: Color? = null,
): NodeValue =
    TextTransform(
        TextTransformData(size, weight, style, decoration, case, variant, color),
        content.children,
    ).wrappedAsValue()

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
