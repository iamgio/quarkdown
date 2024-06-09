package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable

/**
 * A builder of inline CSS rules.
 */
class CssBuilder {
    /**
     * Key-value CSS entries.
     */
    private val entries = mutableMapOf<String, String>()

    /**
     * Pushes a key-value CSS entry.
     * @param key CSS entry key
     * @param value CSS entry value
     * @return this for concatenation
     */
    fun entry(
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
    fun entry(
        key: String,
        value: RenderRepresentable?,
    ) = entry(key, value?.asCSS)

    /**
     * @return a string representation of the CSS entries contained within this builder
     */
    fun build() = entries.entries.joinToString(separator = " ") { "${it.key}: ${it.value};" }
}
