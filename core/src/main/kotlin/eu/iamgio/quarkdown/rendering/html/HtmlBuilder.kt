package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.NodeVisitor

private const val INDENT = "  "

/**
 *
 */
class HtmlBuilder(private val name: String, private val renderer: NodeVisitor<CharSequence>) {
    private val builders = mutableListOf<HtmlBuilder>()
    private val attributes = mutableMapOf<String, Any>()
    private var content = StringBuilder()
    private var isVoid = false

    fun attribute(
        key: String,
        value: String,
    ) = apply { this.attributes[key] = value }

    fun void(isVoid: Boolean) = apply { this.isVoid = isVoid }

    fun tag(
        name: String,
        init: HtmlBuilder.() -> Unit = {},
    ) = renderer.tagBuilder(name, init).also { builders += it }

    fun build(): String =
        buildString {
            // Opening tag.
            append("<")
            append(name)
            attributes.entries.forEach { (key, value) ->
                append(" $key=\"$value\"")
            }
            append(">")

            if (isVoid) {
                return@buildString
            }

            // append("\n")

            content.lineSequence()
                .filterNot { it.isEmpty() }
                .forEach { append("\n").append(INDENT).append(it) }

            append("\n")

            // Indented inner content.
            builders.forEach { builder ->
                builder.build()
                    .lineSequence()
                    .filterNot { it.isEmpty() }
                    .forEach {
                        append(INDENT).append(it).append("\n")
                    }
            }

            // Closing tag.
            append("</")
            append(name)
            append(">")
        }

    operator fun String.unaryPlus() {
        content.append(this).append("\n")
    }

    operator fun List<Node>.unaryPlus() {
        forEach {
            // content.append(INDENT)
            content.append(it.accept(renderer))
            content.append("\n")
        }
    }
}

fun NodeVisitor<CharSequence>.tagBuilder(
    name: String,
    init: HtmlBuilder.() -> Unit,
) = HtmlBuilder(name, renderer = this).also(init)
