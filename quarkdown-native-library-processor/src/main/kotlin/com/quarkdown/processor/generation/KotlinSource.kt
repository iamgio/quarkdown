package com.quarkdown.processor.generation

/**
 * Indent-aware builder for emitted Kotlin source code.
 */
internal class KotlinSource {
    private val buffer = StringBuilder()
    private var depth = 0

    /** Emits [text] indented at the current depth, followed by a newline. Blank text emits just the newline. */
    fun line(text: String = "") {
        if (text.isNotEmpty()) repeat(depth) { buffer.append('\t') }
        buffer.append(text)
        buffer.append('\n')
    }

    /** Emits an empty line. */
    fun blank(): Unit = line()

    /** Emits each line of [text] separately at the current depth. Used for multi-line KDoc / import blocks. */
    fun lines(text: String): Unit = text.lineSequence().forEach(::line)

    /** Executes [body] with one extra level of indentation. Restores the previous depth even if [body] throws. */
    fun indent(body: KotlinSource.() -> Unit) {
        depth++
        try {
            this.body()
        } finally {
            depth--
        }
    }

    /**
     * Emits `header`, then indents [body], then emits `closer`.
     *
     * ```
     * block("object Foo {") { line("val x = 1") }
     *
     * // object Foo {
     * //     val x = 1
     * // }
     * ```
     */
    fun block(
        header: String,
        closer: String = "}",
        body: KotlinSource.() -> Unit,
    ) {
        line(header)
        indent(body)
        line(closer)
    }

    /**
     * Emits `header`, then one indented `item,` per element, then `closer`.
     * Convenient for Kotlin lists that span multiple lines with trailing commas allowed.
     */
    fun commaList(
        header: String,
        items: Iterable<String>,
        closer: String,
    ) {
        line(header)
        indent { items.forEach { line("$it,") } }
        line(closer)
    }

    override fun toString(): String = buffer.toString()
}
