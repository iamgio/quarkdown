package com.quarkdown.core.lexer.scan

/**
 * Escapes the character that follows it, wherever escapes are allowed.
 */
internal const val ESCAPE = '\\'

/**
 * A cursor over a bounded slice of a source.
 * @param source the text to scan
 * @param limit index just past the last character the scanner may read
 * @param index index of the character the scanner points at
 */
class Scanner(
    val source: CharSequence,
    val limit: Int = source.length,
    var index: Int = 0,
) {
    /**
     * Whether the scanner has reached its limit.
     */
    val isAtEnd: Boolean get() = index >= limit

    /**
     * @param offset characters past the current index
     * @return the character at [offset], or `null` outside the slice
     */
    fun peek(offset: Int = 0): Char? = (index + offset).takeIf { it in 0 until limit }?.let(source::get)

    /**
     * Reads the neighbouring character even when it lies before the slice's own start, which is what a
     * boundary rule needs.
     * @return the character before the current index, or `null` at the source's start
     */
    fun previous(): Char? = source.getOrNull(index - 1)

    /**
     * @return the current index, to be given back to [reset] or [sliceFrom]
     */
    fun mark(): Int = index

    /**
     * @param mark an index the scanner passed
     * @return the text between [mark] and the current index
     */
    fun sliceFrom(mark: Int): String = source.substring(mark, index)

    /**
     * @return whether [char] was next and has been consumed
     */
    fun take(char: Char): Boolean = (peek() == char).also { if (it) index++ }

    /**
     * @return whether [text] was next and has been consumed
     */
    fun take(
        text: String,
        ignoreCase: Boolean = false,
    ): Boolean {
        if (index + text.length > limit || !source.startsWith(text, index, ignoreCase)) return false
        index += text.length
        return true
    }

    /**
     * @return whether the next character satisfied [predicate] and has been consumed
     */
    fun takeIf(predicate: (Char) -> Boolean): Boolean {
        if (peek()?.let(predicate) != true) return false
        index++
        return true
    }

    /**
     * Consumes every character satisfying [predicate].
     * @return how many were consumed
     */
    fun takeWhile(predicate: (Char) -> Boolean): Int {
        val mark = index
        while (index < limit && predicate(source[index])) index++
        return index - mark
    }

    /**
     * @return the length of the run of [char] starting here, possibly zero
     */
    fun takeRun(char: Char): Int = takeWhile { it == char }

    /**
     * @return how many spaces and tabs were consumed
     */
    fun takeSpacesAndTabs(): Int = takeWhile { it == ' ' || it == '\t' }

    /**
     * Consumes everything up to the limit.
     * @return the consumed text
     */
    fun takeRest(): String {
        val mark = index
        index = limit
        return source.substring(mark, limit)
    }

    /**
     * Runs [block], keeping what it consumed when it returns `true` and rewinding when it returns `false`.
     * @return what [block] returned
     */
    fun step(block: Scanner.() -> Boolean): Boolean {
        val mark = index
        if (block()) return true
        index = mark
        return false
    }

    /**
     * Repeats [block] as a [step] while it succeeds, so a failed iteration consumes nothing. An iteration
     * that succeeds without consuming anything ends the loop and is not counted, because repeating it could
     * never terminate.
     * @return how many times it succeeded while making progress
     */
    fun repeatWhile(block: Scanner.() -> Boolean): Int {
        var count = 0
        while (true) {
            val mark = index
            if (!step(block)) break
            if (index == mark) break
            count++
        }
        return count
    }

    /**
     * Runs [block], keeping what it consumed when it returns a value and rewinding when it returns `null`.
     * @return what [block] returned
     */
    fun <T : Any> attempt(block: Scanner.() -> T?): T? {
        val mark = index
        val result = block()
        if (result == null) index = mark
        return result
    }

    /**
     * Runs [block] and always rewinds, so the scanner only looks and never consumes.
     * @return what [block] returned
     */
    fun <T> lookahead(block: Scanner.() -> T): T {
        val mark = index
        try {
            return block()
        } finally {
            index = mark
        }
    }

    /**
     * Consumes one character.
     * @return whether there was one to consume
     */
    fun advance(): Boolean = takeIf { true }
}

/**
 * Consumes a backslash and the character it escapes, which may not be a line terminator.
 * @return whether an escape was consumed
 */
fun Scanner.takeEscape(): Boolean = step { take(ESCAPE) && takeIf { it != '\n' } }

/**
 * @param from index the scanner starts at, the line's first character by default
 * @param to index just past the last character the scanner may read, the line's end by default
 * @return a scanner over a slice of the line, its terminator excluded
 */
fun Line.scanner(
    from: Int = start,
    to: Int = end,
): Scanner = Scanner(sourceOf(), limit = to, index = from)

/**
 * @return a scanner over the line's content, its indentation and terminator excluded
 */
fun Line.contentScanner(): Scanner = scanner(from = contentStart)
