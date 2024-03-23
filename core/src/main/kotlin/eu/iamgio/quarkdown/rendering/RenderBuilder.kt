package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.SystemProperties
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.isPrettyOutputEnabled

/**
 * A builder of a generic output code.
 * Custom `tag` methods that allow nesting builders must be provided by
 * the implementations, in order to allow type matching between sub-builders their parent builder,
 * by using a DSL-like approach.
 * @param name name of the root tag
 * @param renderer node renderer, used to add nodes directly to the code
 * @param pretty whether the output code should be pretty
 * @see tagBuilder
 * @see eu.iamgio.quarkdown.rendering.html.HtmlBuilder
 */
abstract class RenderBuilder(
    private val name: String,
    private val renderer: NodeRenderer<*>,
    private val pretty: Boolean,
) {
    /**
     * Sub-builders for nested tags.
     */
    protected val builders = mutableListOf<RenderBuilder>()

    /**
     * Text content of the tag.
     */
    protected val content = StringBuilder()

    /**
     * @return this builder and its nested content into stringified HTML code.
     */
    abstract fun build(): String

    /**
     * Appends a raw string content to this builder's content buffer.
     * @param content string content to append
     */
    abstract fun append(content: CharSequence)

    /**
     * Appends a string value to this tag's content.
     * Usage: `+"Some string"`
     */
    operator fun CharSequence.unaryPlus() {
        append(this)
    }

    /**
     * Appends a node to this tag's content.
     * Their string representation is given by this [RenderBuilder]'s [renderer].
     * Usage: `+someNode`
     */
    operator fun Node.unaryPlus() {
        +this.accept(renderer)
    }

    /**
     * Appends a sequence of nodes to this tag's content.
     * Their string representation is given by this [RenderBuilder]'s [renderer].
     * Usage: `+someNode.children`
     */
    operator fun List<Node>.unaryPlus() {
        forEach { +it }
    }
}

/**
 * Creates a generic builder.
 * Example:
 * ```
 * tagBuilder("name") {
 *     +content
 * }
 * .someOption()
 * .build()
 * ```
 *
 * @param name tag name
 * @param pretty whether the output code should be pretty
 * @param init action to run at initialization
 * @return the new builder
 */
fun <B : RenderBuilder> NodeRenderer<B>.tagBuilder(
    name: String,
    pretty: Boolean = SystemProperties.isPrettyOutputEnabled,
    init: B.() -> Unit = {},
) = createBuilder(name, pretty).also(init)

/**
 * A quick way to create a simple tag builder.
 * Example:
 * ```
 * tagBuilder("name", content)
 *     .someOption()
 *     .build()
 * ```
 * @param name tag name
 * @param content nodes to render as HTML within the tag
 * @return the new builder
 * @see tagBuilder
 */
fun <B : RenderBuilder> NodeRenderer<B>.tagBuilder(
    name: String,
    content: List<Node>,
) = tagBuilder(name) { +content }

/**
 * Builds a tag.
 * Example:
 * ```
 * buildTag("name") {
 *     +content
 * }
 * ```
 * @param name tag name
 * @param init action to run at initialization
 * @return output code of the tag
 * @see tagBuilder
 */
fun <B : RenderBuilder> NodeRenderer<B>.buildTag(
    name: String,
    init: B.() -> Unit,
) = tagBuilder(name, init = init).build()

/**
 * A quick way to build a simple tag.
 * Example:
 * ```
 * buildTag("name", content)
 * ```
 * @param name tag name
 * @param content nodes to render to output code within the tag
 * @return output code of the tag
 * @see buildTag
 */
fun <B : RenderBuilder> NodeRenderer<B>.buildTag(
    name: String,
    content: List<Node>,
) = buildTag(name) { +content }

/**
 * A quick way to build a simple tag.
 * Example:
 * ```
 * buildTag("name", "content")
 * ```
 * @param name tag name
 * @param content string content of the tag
 * @return output code of the tag
 * @see buildTag
 */
fun <B : RenderBuilder> NodeRenderer<B>.buildTag(
    name: String,
    content: String,
) = buildTag(name) { +content }
