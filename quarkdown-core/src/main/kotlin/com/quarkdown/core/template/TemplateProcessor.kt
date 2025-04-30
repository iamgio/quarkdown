package com.quarkdown.core.template

import com.quarkdown.core.rendering.template.TemplatePlaceholders
import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.core.util.replace

/**
 * A builder-like processor for a simple template engine with three basic features:
 *
 * - **Values**: replace a placeholder in the template with a value.
 *   Values in templates are wrapped by double square brackets: `[[NAME]]`.
 *
 * - **Conditionals**: show or hide fragments of the template code.
 *   The fragment in the template must be fenced by `[[if:NAME]]` and `[[endif:NAME]]`.
 *   An inverted (_not_) placeholder is fenced by `[[if:!NAME]]` and `[[endif:!NAME]]`.
 *
 * - **Iterables**: repeat the content in their fragment as many times as the iterable's size,
 *   while replacing the placeholder with the current item during each iteration.
 *   The fragment in the template must be fenced by `[[for:NAME]]` and `[[endfor:NAME]]`,
 *   and the iterated value appears like a normal value: `[[NAME]]`.
 *
 * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
 * An example template resource can be found in `resources/render/html-wrapper.html.template`
 *
 * @param text text or code of the template
 */
class TemplateProcessor(
    private val text: String,
    private val values: MutableMap<String, Any> = mutableMapOf(),
    private val conditionals: MutableMap<String, Boolean> = mutableMapOf(),
    private val iterables: MutableMap<String, Iterable<Any>> = mutableMapOf(),
) {
    /**
     * Adds a reference to a placeholder in the template code.
     * The placeholder in the template must be wrapped by double square brackets.
     * @param placeholder placeholder to replace
     * @param value value to replace in change of [placeholder]
     * @return this for concatenation
     */
    fun value(
        placeholder: String,
        value: Any,
    ) = apply { values[placeholder] = value }

    /**
     * Adds a conditional variable that shows or removes fragments of the template code.
     * The fragment in the template must be fenced by `[[if:NAME]]` and `[[endif:NAME]]`.
     * An inverted (_not_) placeholder is also injected and can be accessed via `[[if:!NAME]]`.
     * @param conditional conditional name
     * @param value whether the fragment should be shown (`true`) or hidden (`false`)
     * @return this for concatenation
     */
    fun conditional(
        conditional: String,
        value: Boolean,
    ) = apply {
        conditionals[conditional] = value
        conditionals["!$conditional"] = !value // inverted conditional ("not")
    }

    /**
     * Adds both a [conditional] to check if [value] is not `null`,
     * and a [value] to replace the placeholder with the non-`null` value.
     * @param placeholder both placeholder to replace and name of the conditional
     * @param value value to replace in change of the placeholder
     * @return this for concatenation
     * @see conditional
     * @see value
     */
    fun optionalValue(
        placeholder: String,
        value: Any?,
    ) = conditional(placeholder, value != null).value(placeholder, value ?: "")

    /**
     * Adds an iterable to replace a placeholder in the template code.
     * The placeholder in the template must be fenced by `[[for:NAME]]` and `[[endfor:NAME]]`,
     * and the iterated value appears like a normal value: `[[NAME]]`.
     * @param placeholder placeholder to replace
     * @param iterable iterable to replace in change of [placeholder]
     * @return this for concatenation
     */
    fun iterable(
        placeholder: String,
        iterable: Iterable<Any>,
    ) = apply { iterables[placeholder] = iterable }

    /**
     * Creates a copy of this template processor with the same injected properties.
     * @param text new text the template
     * @return a new [TemplateProcessor] with the same injections, and the new text
     */
    fun copy(text: String) =
        TemplateProcessor(
            text,
            values.toMutableMap(),
            conditionals.toMutableMap(),
            iterables.toMutableMap(),
        )

    /**
     * Adds a reference to a content placeholder in the template code.
     * This is used to inject rendered code in a template.
     * @param content value to replace in change of the `\[\[CONTENT]]` placeholder
     * @return this for concatenation
     */
    fun content(content: CharSequence) = value(TemplatePlaceholders.CONTENT, content)

    /**
     * Creates a regex to find the fragment of a given placeholder and keyword.
     * @param keyword name of the keyword, such as `if` or `for`
     * @param placeholder name of the placeholder associated with the actual content
     * @param trimStart whether to trim leading whitespace after the beginning of the fragment
     * @param trimEnd whether to trim trailing whitespace before the end of the fragment
     * @return a regex that matches the conditional fragments between `[[keyword:PLACEHOLDER]]` and `[[endkeyword:PLACEHOLDER]]`
     */
    private fun createFragmentRegex(
        keyword: String,
        placeholder: String,
        trimStart: Boolean = true,
        trimEnd: Boolean = true,
    ) = (
        "\\[\\[$keyword:$placeholder]]" + // Start
            (if (trimStart) "(?:\\R\\s*)?" else "") + // Trim start
            "((.|\\R)+?)" + // Content
            (if (trimEnd) "(?:\\R\\s*)?" else "") + // Trim end
            "\\[\\[end$keyword:$placeholder]]" // End
    ).toRegex(RegexOption.MULTILINE)

    /**
     * @return a regex that matches the conditional fragments between `[[if:PLACEHOLDER]]` and `[[endif:PLACEHOLDER]]`
     */
    private fun createConditionalRegex(placeholder: String) = createFragmentRegex("if", placeholder)

    /**
     * @return a regex that matches the conditional fragments between `[[for:PLACEHOLDER]]` and `[[endfor:PLACEHOLDER]]`
     */
    private fun createIterableRegex(placeholder: String) = createFragmentRegex("for", placeholder, trimStart = false)

    /**
     * Starting from [index], goes backwards through whitespaces to find the first non-whitespace character.
     * The result is the final index, stopping at the last newline character encountered (excluded).
     *
     * Example (`*` is the start index, `|` is the result index):
     * ```
     * Line 1
     * |
     *    *Line 2
     * ```
     *
     * By removing this whitespace, the result would be:
     * ```
     * Line 1
     * Line 2
     * ```
     *
     * In the following example, no trimming is performed because no newline is met:
     * ```
     * Line 1   *Line 2
     * ```
     *
     * Example in context (see [replaceConditionals]):
     * ```
     * [[if:A]]Line 1[[endif:A]]
     * [[if:B]]Line 2[[endif:B]]
     * [[if:C]]Line 3[[endif:C]]
     * [[if:D]]Line 4[[endif:D]]
     * ```
     *
     * Suppose `A`=true, `B`=false, `C`=false, `D`=true.
     * Without this operation, the result would be:
     * ```
     * Line 1
     *
     *
     * Line 4
     * ```
     *
     * With this operation, the result is:
     * ```
     * Line 1
     * Line 4
     * ```
     *
     * @param index index to start from
     * @return the index of the first non-whitespace character before a newline
     */
    private fun CharSequence.findStartTrimmedIndexToNewline(index: Int): Int {
        var start = index
        var offset = 0
        while (start > offset++ && this[start - offset].isWhitespace()) {
            if (this[start - offset] == '\n') {
                start -= offset
                offset = 0
            }
        }
        return start
    }

    /**
     * Processes conditionals in the template by keeping or cutting content depending on their boolean value.
     */
    private fun StringBuilder.replaceConditionals() {
        // Just like ifdef macros, conditionals keep or cut content depending on a boolean value.
        // Delimiters are defined as [[if:NAME]]...[[endif:NAME]] in the template files.
        conditionals.forEach { (placeholder, isTrue) ->
            // If there is a match with the regex:
            // Keep the inner content (without the delimiters) if the conditional value is true, remove it otherwise.
            createConditionalRegex(placeholder)
                .findAll(this)
                .sortedByDescending { it.range.first } // Iterate backwards to avoid index mismatches after replacing.
                .forEach { match ->
                    // Text to replace the fragment with. Group [1] is the inner content without the delimiters.
                    val replacement = match.groups[1]?.value?.takeIf { isTrue } ?: ""
                    // Start index of the fragment to replace.
                    // If the fragment is to be removed (condition is false), leading whitespace is trimmed.
                    val start = match.range.first.let { if (isTrue) it else findStartTrimmedIndexToNewline(it) }

                    replace(
                        start,
                        match.range.last + 1,
                        replacement,
                    )
                }
        }
    }

    /**
     * Processes iterables in the template by repeating the content in their fragment as many times as the iterable's size,
     * while replacing the placeholder with the current item during each iteration.
     *
     * Example:
     * ```
     * [[for:ITEM]][[ITEM]] [[endfor:ITEM]]
     * ```
     * Output on `[a, b, c]`: `a b c `
     */
    private fun StringBuilder.replaceIterables() {
        iterables.forEach { (placeholder, iterable) ->
            createIterableRegex(placeholder)
                .findAll(this)
                .sortedByDescending { it.range.first }
                .forEach { match ->
                    // Repeat the inner content for each item in the iterable, replacing the placeholder with the item.
                    val replacement =
                        iterable.joinToString(separator = "") { item ->
                            match.groups[1]?.value?.replace("[[$placeholder]]", item.toString()) ?: ""
                        }
                    val start = findStartTrimmedIndexToNewline(match.range.first)
                    replace(
                        start,
                        match.range.last + 1,
                        replacement,
                    )
                }
        }
    }

    /**
     * Processes values in the template by replacing their placeholder with their corresponding value.
     */
    private fun StringBuilder.replaceValues() {
        values.forEach { (placeholder, value) ->
            replace("[[$placeholder]]", value.toString())
        }
    }

    /**
     * @return the original template [text], with all placeholders and conditionals processed into the final output
     */
    fun process(): CharSequence =
        buildString {
            append(text.normalizeLineSeparators())
            replaceConditionals()
            replaceIterables()
            replaceValues()
        }

    companion object {
        /**
         * @param name name of the internal resource
         * @param referenceClass reference classpath to use to retrieve the internal resource
         * @return a new [TemplateProcessor] with its template loaded from the resource content
         * @throws IllegalStateException if the resource cannot be found
         */
        fun fromResourceName(
            name: String,
            referenceClass: Class<*> = TemplateProcessor::class.java,
        ) = TemplateProcessor(
            referenceClass
                .getResourceAsStream(name)
                ?.reader()
                ?.readText()
                ?: throw IllegalStateException("Cannot find wrapper resource $name."),
        )
    }
}
