package com.quarkdown.rendering.html.css

import com.quarkdown.core.rendering.representable.RenderRepresentable

/**
 * A builder of CSS declarations (property-value pairs), with null-safe insertion.
 *
 * Supports two output formats:
 * - [build] produces a single-line inline style (e.g. for the `style` HTML attribute).
 * - [buildBlock] produces an indented, multi-line block (e.g. for a stylesheet rule body).
 */
class CssBuilder {
    /**
     * Key-value CSS entries.
     */
    private val entries = mutableMapOf<String, String>()

    /**
     * Pushes a key-value CSS entry as long as [value] is not `null`.
     * @param key CSS entry key
     * @param value CSS entry value
     * @return this for concatenation
     */
    private fun entry(
        key: String,
        value: String?,
    ) = apply {
        value?.let { entries[key] = it }
    }

    /**
     * Pushes a key-value CSS entry.
     * @param key CSS entry key
     * @param value CSS entry value
     * @return this for concatenation
     */
    private fun entry(
        key: String,
        value: RenderRepresentable?,
    ) = entry(key, value?.asCSS)

    /**
     * Shorthand syntactic sugar for [entry].
     * @see entry
     */
    infix fun String.value(value: String?) = entry(this, value)

    /**
     * Shorthand syntactic sugar for [entry].
     * @see entry
     */
    infix fun String.value(value: RenderRepresentable?) = entry(this, value)

    /**
     * Like [value], but appends `!important` to the declaration.
     * @see value
     */
    infix fun String.importantValue(value: String?) = entry(this, value?.let { "$it !important" })

    /**
     * Like [value], but appends `!important` to the declaration.
     * @see value
     */
    infix fun String.importantValue(value: RenderRepresentable?) = importantValue(value?.asCSS)

    /**
     * @return a single-line string representation of the CSS entries, suitable for inline styles
     */
    fun build() = entries.entries.joinToString(separator = " ") { "${it.key}: ${it.value};" }

    /**
     * @return an indented, multi-line string representation of the CSS entries,
     *         suitable for a rule body in a stylesheet block
     */
    fun buildBlock(indent: String = "    ") =
        entries.entries.joinToString(separator = "\n") { "$indent${it.key}: ${it.value};" }
}

/**
 * Example usage:
 * ```
 * val css = css {
 *   "color" value Color(255, 0, 0)
 *   "font-size" value Size(16, SizeUnit.PX)
 * }
 * ```
 * @return a string representation of CSS entries contained within the builder.
 */
fun css(init: CssBuilder.() -> Unit): String = CssBuilder().apply(init).build()
