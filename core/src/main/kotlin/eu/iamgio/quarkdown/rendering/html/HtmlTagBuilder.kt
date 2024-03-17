package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Node

/**
 *
 */
class HtmlTagBuilder(private val name: String) {
    private var content: CharSequence = ""
    private val attributes = mutableMapOf<String, Any>()
    private var isEmpty = false

    fun content(content: CharSequence) = apply { this.content = content }

    fun attribute(
        key: String,
        value: String,
    ) = apply { this.attributes[key] = value }

    fun empty(isEmpty: Boolean) = apply { this.isEmpty = isEmpty }

    fun build(): String {
        return buildString {
            // Opening tag.
            append("<")
            append(name)
            if (attributes.isNotEmpty()) {
                val attributesString =
                    attributes.entries.joinToString(separator = " ") { (key, value) -> "$key=\"$value\"" }
                append(" ")
                append(attributesString)
            }
            if (isEmpty) {
                append(">")
                return@buildString
            }
            append(">\n")

            // Inner content.
            append(content)

            // Closing tag.
            append("\n</")
            append(name)
            append(">")
        }
    }
}

fun HtmlNodeRenderer.tag(
    name: String,
    content: () -> List<Node>,
) = HtmlTagBuilder(name).content(
    content().joinToString(separator = "\n") { it.accept(this) },
)

fun HtmlNodeRenderer.rawTag(
    name: String,
    content: () -> String,
) = HtmlTagBuilder(name).content(content())

fun HtmlNodeRenderer.tag(
    name: String,
    vararg children: HtmlTagBuilder,
) = HtmlTagBuilder(name).content(children.joinToString(separator = "\n") { it.build() })

fun HtmlNodeRenderer.tag(name: String) = HtmlTagBuilder(name).empty(false)
