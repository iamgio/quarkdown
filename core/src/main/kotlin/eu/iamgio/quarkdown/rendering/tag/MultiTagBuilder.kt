package eu.iamgio.quarkdown.rendering.tag

/**
 * Builder of multiple sub-tags from a single source.
 * @param renderer renderer of the sub-tags
 * @see TagBuilder
 * @see buildMultiTag
 */
class MultiTagBuilder(
    renderer: TagNodeRenderer<*>,
    private val pretty: Boolean,
) : TagBuilder(name = "", renderer, pretty) {
    override fun build() = content.toString()

    override fun append(content: CharSequence) {
        if (pretty && super.content.isNotEmpty()) super.content.append("\n")
        super.content.append(content)
    }
}

/**
 * Creates a multi-tag builder.
 *
 * Example usage:
 *
 * ```
 * buildMultiTag {
 *     +buildTag("x") { ... }
 *     +buildTag("y") { ... }
 * }
 * ```
 */
fun TagNodeRenderer<*>.buildMultiTag(init: MultiTagBuilder.() -> Unit) =
    MultiTagBuilder(
        renderer = this,
        pretty = this.pretty,
    ).also(init).build()
