package com.quarkdown.processor.generation

/**
 * Text-level rewriter that keeps a KDoc block coherent with the wrapper's exported names.
 *
 * Downstream tools (Quarkdoc / Dokka) read the wrapper's KDoc without any awareness of the
 * source-level `@Name` renames, so any identifier in the wrapper's KDoc must already refer to
 * the wrapper's parameter list. Two forms need substitution:
 *
 * - `@param originalName ...` at the head of a param tag;
 * - `[originalName]` link references anywhere in the text.
 *
 * All substitutions happen in a single pass over each pattern, so no rename can be re-fed into a
 * later rename in the same map even when the exported name of one parameter collides with the
 * original name of another (rare but possible when a parameter is renamed to another's source
 * identifier).
 *
 * The rewriter operates purely on the raw KDoc string; it does not know about paragraph structure,
 * indentation, or line boundaries, so it is safe on both single-line and multi-line KDoc blocks.
 */
internal object KDocRewriter {
    /**
     * Returns [rawKDoc] with every `@param name` and `[name]` occurrence rewritten to its exported
     * counterpart in [renames], leaving any unlisted name unchanged. A no-op when [renames] is
     * empty or contains only identity mappings.
     */
    fun rewrite(
        rawKDoc: String,
        renames: Map<String, String>,
    ): String {
        val effective = renames.filter { (source, exported) -> source != exported }
        if (effective.isEmpty()) return rawKDoc

        val alternation = effective.keys.joinToString("|") { Regex.escape(it) }
        val paramPattern = Regex("""(@param\s+)($alternation)\b""")
        val linkPattern = Regex("""\[($alternation)]""")

        return rawKDoc
            .replace(paramPattern) { match -> "${match.groupValues[1]}${effective.getValue(match.groupValues[2])}" }
            .replace(linkPattern) { match -> "[${effective.getValue(match.groupValues[1])}]" }
    }

    /**
     * Returns every `@param ...` block from [rawKDoc], each including any continuation lines that
     * belong to that tag (a continuation is a line that does not start a new `@` tag). Description
     * paragraphs before the first tag and any non-`@param` tags are dropped.
     *
     * Used to lift the primary-constructor parameter documentation off a `@Spread` data class and
     * splice it into the wrapper's KDoc, without carrying the class's own description into the
     * function documentation.
     */
    fun extractParamTags(rawKDoc: String): List<String> = collectParamBlocks(rawKDoc) { true }

    /**
     * Returns [rawKDoc] with every `@param name ...` block whose name is in [names] removed. A
     * block spans its `@param` line plus any continuation lines up to (but not including) the next
     * tag. Used to drop `@Spread` parameter entries from the source KDoc: they name a symbol that
     * does not exist on the generated wrapper.
     */
    fun stripParamTags(
        rawKDoc: String,
        names: Set<String>,
    ): String {
        if (names.isEmpty()) return rawKDoc
        val lines = rawKDoc.lines()
        val out = mutableListOf<String>()
        var dropping = false
        for (line in lines) {
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("@param") -> {
                    dropping = paramName(trimmed) in names
                    if (!dropping) out += line
                }
                trimmed.startsWith("@") -> {
                    dropping = false
                    out += line
                }
                !dropping -> out += line
            }
        }
        return out.joinToString("\n")
    }

    private inline fun collectParamBlocks(
        rawKDoc: String,
        keep: (name: String) -> Boolean,
    ): List<String> {
        val lines = rawKDoc.lines()
        val blocks = mutableListOf<MutableList<String>>()
        var current: MutableList<String>? = null

        for (line in lines) {
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("@param") -> {
                    current =
                        if (keep(paramName(trimmed))) {
                            mutableListOf(line).also { blocks += it }
                        } else {
                            null
                        }
                }
                trimmed.startsWith("@") -> {
                    current = null
                }
                current != null -> current += line
            }
        }
        return blocks.map { it.joinToString("\n").trimEnd() }
    }

    /**
     * Parses the parameter name from a line that starts with `@param`. Handles arbitrary whitespace
     * (spaces and tabs) between `@param` and the name, and between the name and the description.
     */
    private fun paramName(trimmedLine: String): String =
        trimmedLine
            .removePrefix("@param")
            .trimStart()
            .takeWhile { !it.isWhitespace() }
}
