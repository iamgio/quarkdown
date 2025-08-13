package com.quarkdown.grammargen.textmate

import com.quarkdown.core.lexer.patterns.QuarkdownBlockTokenRegexPatterns
import com.quarkdown.core.lexer.patterns.QuarkdownInlineTokenRegexPatterns
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.core.util.Escape
import com.quarkdown.grammargen.GrammarFormat

private const val TEMPLATE = "/quarkdown.tmLanguage.json.template"

/**
 * Grammar format implementation for generating TextMate grammar definitions for VS Code.
 */
class TextMateFormat : GrammarFormat<TextMatePattern> {
    override val baseTemplateProcessor: TemplateProcessor
        get() = TemplateProcessor.fromResourceName(TEMPLATE, referenceClass = javaClass)

    override fun createPatterns(): List<TextMatePattern> {
        val block = QuarkdownBlockTokenRegexPatterns()
        val inline = QuarkdownInlineTokenRegexPatterns()
        return listOf(
            block.heading textMate "markup.heading.markdown",
            block.blockQuote textMate "markup.quote.markdown",
            block.horizontalRule textMate "meta.separator.markdown",
            inline.strongAsterisk textMate "markup.bold.markdown",
            inline.strongUnderscore textMate "markup.bold.markdown",
            inline.emphasisAsterisk textMate "markup.italic.markdown",
            inline.emphasisUnderscore textMate "markup.italic.markdown",
            inline.strikethrough textMate "markup.deleted.markdown",
        )
    }

    override fun patternToSource(
        pattern: TextMatePattern,
        isLast: Boolean,
    ): String =
        buildString {
            append("{ ")
            append("\"name\": \"${pattern.name}\"")
            append(", ")
            append("\"match\": \"${Escape.Json.escape(pattern.pattern.regex.pattern)}\"")
            if (pattern.captures.isNotEmpty()) {
                append(", ")
                append("\"captures\": { ")
                append(
                    pattern.captures.joinToString(", ") { capture ->
                        "\"${capture.index}\": { \"name\": \"${capture.name}\" }"
                    },
                )
                append(" }")
            }
            append(" }")
            if (!isLast) append(",")
        }
}
