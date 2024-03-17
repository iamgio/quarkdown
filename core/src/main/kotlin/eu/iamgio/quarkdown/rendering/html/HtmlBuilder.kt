package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.NodeVisitor
import eu.iamgio.quarkdown.util.indent

private const val INDENT = "  "

/**
 * A builder of an HTML tag.
 * @param name name of the root tag
 * @param renderer node renderer, used to add nodes directly to the code
 * @see tagBuilder
 */
class HtmlBuilder(private val name: String, private val renderer: NodeVisitor<CharSequence>) {
    /**
     * Sub-builders for nested tags.
     */
    private val builders = mutableListOf<HtmlBuilder>()

    /**
     * Attributes of the tag.
     */
    private val attributes = mutableMapOf<String, Any>()

    /**
     * Text content of the tag.
     */
    private val content = StringBuilder()

    /**
     * Whether the tag does not expect a closing tag.
     */
    private var isVoid = false

    /**
     * Adds an attribute to this tag.
     * @param key attribute key
     * @param value attribute value
     * @return this for concatenation
     */
    fun attribute(
        key: String,
        value: String,
    ) = apply { this.attributes[key] = value }

    /**
     * Sets whether this tag is void.
     * @param isVoid whether the tag does not expect a closing tag
     * @return this for concatenation
     */
    fun void(isVoid: Boolean) = apply { this.isVoid = isVoid }

    /**
     * Appends a sub-tag.
     * @param name name of the tag
     * @param init action to run at initialization
     * @return this for concatenation
     */
    fun tag(
        name: String,
        init: HtmlBuilder.() -> Unit = {},
    ) = renderer.tagBuilder(name, init).also { builders += it }

    /**
     * @return this builder and its nested content into stringified HTML code.
     */
    fun build(): String =
        buildString {
            // Opening tag.
            append("<")
            append(name)
            attributes.entries.forEach { (key, value) ->
                append(" $key=\"$value\"")
            }
            append(">")

            // If this is a void tag, neither content nor closing tag is expected.
            if (isVoid) {
                return@buildString
            }

            append("\n")

            // Indented text content.
            append(content.indent(INDENT))

            // Indented content from inner tags.
            builders.forEach { builder ->
                append(builder.build().indent(INDENT))
            }

            // Closing tag.
            append("</")
            append(name)
            append(">")
        }

    /**
     * Appends a string value to this tag's content.
     */
    operator fun String.unaryPlus() {
        content.append(this).append("\n")
    }

    /**
     * Appends a sequence of nodes to this tag's content.
     * Their string representation is given by this [HtmlBuilder]'s [renderer].
     */
    operator fun List<Node>.unaryPlus() {
        forEach {
            content.append(it.accept(renderer))
            content.append("\n")
        }
    }
}

/**
 * Creates an HTML tag builder.
 *
 * Example:
 * ```
 * val builder = tagBuilder("html") {
 *     tag("head") {
 *         tag("meta")
 *             .attribute("charset", "UTF-8")
 *             .void(true)
 *     }
 *     tag("body") {
 *         +node.children
 *     }
 * ```
 *
 *
 * @param name name of the root tag
 * @param init action to run at initialization
 * @return the new builder
 */
fun NodeVisitor<CharSequence>.tagBuilder(
    name: String,
    init: HtmlBuilder.() -> Unit,
) = HtmlBuilder(name, renderer = this).also(init)
