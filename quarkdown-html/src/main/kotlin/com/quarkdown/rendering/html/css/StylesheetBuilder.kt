package com.quarkdown.rendering.html.css

/**
 * Lightweight DSL for building a CSS stylesheet string.
 * Rule bodies reuse [CssBuilder] for null-safe property insertion.
 */
class StylesheetBuilder {
    private val parts = mutableListOf<String>()

    /** Appends a raw CSS snippet (e.g. `@font-face` or `@import` declarations). */
    fun raw(content: String) {
        parts += content
    }

    /** Appends a CSS rule block with one or more [selectors]. */
    fun rule(
        vararg selectors: String,
        block: CssBuilder.() -> Unit,
    ) {
        val body = CssBuilder().apply(block).buildBlock()
        parts += "${selectors.joinToString(",\n")} {\n$body}"
    }

    fun build(): String = parts.joinToString("\n\n")
}

/**
 * Example usage:
 * ```
 * val css = stylesheet {
 *   raw("@import url('fonts.css');")
 *   rule("body") {
 *     "color" value Color(0, 0, 0)
 *     "font-size" value Size(16, SizeUnit.PX)
 *   }
 * }
 * ```
 * @return a string representation of a CSS stylesheet built within the given [block]
 */
fun stylesheet(block: StylesheetBuilder.() -> Unit): String = StylesheetBuilder().apply(block).build()
