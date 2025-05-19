package com.quarkdown.rendering.html

import com.quarkdown.core.ast.Node
import com.quarkdown.core.rendering.html.BaseHtmlNodeRenderer
import com.quarkdown.core.rendering.tag.TagBuilder
import com.quarkdown.core.rendering.tag.tagBuilder
import com.quarkdown.core.util.indent
import com.quarkdown.rendering.html.css.CssBuilder
import com.quarkdown.rendering.html.css.css

/**
 * String used to indent nested code.
 */
private const val INDENT = "    "

/**
 * A builder of an HTML tag.
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
 * @see TagBuilder
 * @see tagBuilder
 */
class HtmlTagBuilder(
    private val name: String,
    private val renderer: BaseHtmlNodeRenderer,
    private val pretty: Boolean,
) : TagBuilder(name, renderer, pretty) {
    /**
     * Attributes of the tag.
     */
    private val attributes = mutableMapOf<String, Any>()

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
        value: Any,
    ) = apply { this.attributes[key] = value }

    /**
     * Adds an attribute to this tag only if [value] is not `null`.
     * @param key attribute key
     * @param value attribute value, applied only if not `null`
     * @return this for concatenation
     */
    fun optionalAttribute(
        key: String,
        value: Any?,
    ) = apply {
        if (value != null) attribute(key, value)
    }

    /**
     * Sets whether this tag is void (without a closing tag).
     * @param isVoid whether the tag does not expect a closing tag
     * @return this for concatenation
     */
    fun void(isVoid: Boolean) = apply { this.isVoid = isVoid }

    /**
     * Applies a CSS style via the `style` attribute to this tag.
     * The attribute is _not_ added if the generated CSS string is empty.
     * @param init CSS builder initialization
     * @return this for concatenation
     * @see com.quarkdown.rendering.html.css.css
     */
    fun style(init: CssBuilder.() -> Unit) = optionalAttribute("style", css(init).takeUnless { it.isEmpty() })

    /**
     * Applies a single class name via the `class` attribute to this tag.
     * @param className class name. The attribute is not applied if it's `null`
     * @return this for concatenation
     * @see optionalAttribute
     */
    fun className(className: String?) = optionalAttribute("class", className)

    /**
     * Applies a sequence of class names via the `class` attribute to this tag.
     * @param classNames class names. `null` elements are ignored. The attribute is not applied if all elements are `null`
     * @return this for concatenation
     */
    fun classNames(vararg classNames: String?) =
        optionalAttribute(
            "class",
            classNames
                .asSequence()
                .filterNotNull()
                .joinToString(separator = " ")
                .takeIf { it.isNotEmpty() },
        )

    /**
     * Adds a `data-hidden` attribute to this tag as a flag that this is a hidden element.
     * A page that has either zero elements or only hidden elements is considered blank.
     * This attribute is usually read by external stylesheets and scripts.
     * @return this for concatenation
     */
    fun hidden() = attribute("data-hidden", "")

    /**
     * @return this builder and its nested content into stringified HTML code.
     */
    override fun build(): String =
        buildString {
            fun CharSequence.indent() = if (pretty) this.indent(INDENT) else this

            // Opening tag.
            append("<")
            append(name)
            attributes.entries.forEach { (key, value) ->
                append(" $key=\"$value\"")
            }

            // If this is a void tag, neither content nor closing tag is expected.
            if (isVoid) {
                append(" />")
                return@buildString
            }

            append(">")

            if (pretty) {
                append("\n")
            }

            // Indented content from inner tags.
            builders.forEach { builder ->
                append(builder.build().indent())
            }

            // Indented text content.
            append(content.indent())

            // Closing tag.
            append("</")
            append(name)
            append(">")
        }

    override fun append(content: CharSequence) {
        super.content.append(content)
        if (pretty) super.content.append("\n")
    }

    /**
     * Appends a sub-tag.
     * @param name name of the tag
     * @param init action to run at initialization
     * @return this for concatenation
     */
    fun tag(
        name: String,
        init: HtmlTagBuilder.() -> Unit = {},
    ) = renderer.tagBuilder(name, pretty, init).also { builders += it }

    /**
     * Appends a sub-tag.
     * @param name name of the tag
     * @param content nodes to render as HTML within the tag
     * @return this for concatenation
     */
    fun tag(
        name: String,
        content: List<Node>,
    ) = tag(name) { +content }
}
