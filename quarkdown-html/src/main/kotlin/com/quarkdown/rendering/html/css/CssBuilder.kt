package com.quarkdown.rendering.html.css

import com.quarkdown.core.rendering.representable.RenderRepresentable

/**
 * A builder of inline CSS rules.
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
     * @return a string representation of the CSS entries contained within this builder
     */
    fun build() = entries.entries.joinToString(separator = " ") { "${it.key}: ${it.value};" }
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
