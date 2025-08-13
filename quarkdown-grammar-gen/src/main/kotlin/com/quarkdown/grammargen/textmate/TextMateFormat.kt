package com.quarkdown.grammargen.textmate

import com.quarkdown.core.lexer.patterns.QuarkdownBlockTokenRegexPatterns
import com.quarkdown.core.lexer.patterns.QuarkdownInlineTokenRegexPatterns
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.grammargen.GrammarFormat
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
            block.heading textMateBlock "markup.heading.markdown",
            block.blockQuote textMateBlock "markup.quote.markdown",
            block.horizontalRule textMateBlock "meta.separator.markdown",
            inline.strongAsterisk textMateInline "markup.bold.markdown" including inlines,
            inline.strongUnderscore textMateInline "markup.bold.markdown",
            inline.emphasisAsterisk textMateInline "markup.italic.markdown",
            inline.emphasisUnderscore textMateInline "markup.italic.markdown",
            inline.strikethrough textMateInline "markup.deleted.markdown",
            // todo add more: https://github.com/microsoft/vscode/blob/main/extensions/markdown-basics/syntaxes/markdown.tmLanguage.json
        )
    }

    private val TextMatePatternType.repositoryName: String
        get() = name.lowercase()

    private fun TextMatePattern.asRepository(): JsonObject =
        buildJsonObject {
            put("name", scope)
            put("match", pattern.regex.pattern)
            if (captures.isNotEmpty()) {
                put(
                    "captures",
                    buildJsonObject {
                        captures.forEach { capture ->
                            put(
                                capture.index.toString(),
                                buildJsonObject {
                                    put("name", capture.scope)
                                },
                            )
                        }
                    },
                )
            }
            if (includes.isNotEmpty()) {
                put(
                    "patterns",
                    buildJsonArray {
                        includes.forEach { include ->
                            addJsonObject {
                                put("include", "#${include.repositoryName}")
                            }
                        }
                    },
                )
            }
        }

    private fun TextMatePattern.asPattern(): JsonObject =
        buildJsonObject {
            put("include", "#$name")
        }

    override fun createContent(): String =
        buildJsonObject {
            val patterns = createPatterns()
            val patternsByType: Map<TextMatePatternType?, List<TextMatePattern>> = patterns.groupBy { it.type }

            put(
                "repository",
                buildJsonObject {
                    addTypedPatternRepository(patternsByType)
                    patterns.forEach { pattern ->
                        put(pattern.name, pattern.asRepository())
                    }
                },
            )
            put(
                "patterns",
                buildJsonArray {
                    patterns.forEach { add(it.asPattern()) }
                },
            )
        }.toString().removeSurrounding("{", "}")

    private fun JsonObjectBuilder.addTypedPatternRepository(patternsByType: Map<TextMatePatternType?, List<TextMatePattern>>) {
        patternsByType.forEach { type, typedPatterns ->
            if (type == null) return@forEach
            put(
                type.repositoryName,
                buildJsonObject {
                    put(
                        "patterns",
                        buildJsonArray {
                            typedPatterns.forEach { pattern ->
                                addJsonObject {
                                    put("include", "#${pattern.name}")
                                }
                            }
                        },
                    )
                },
            )
        }
    }
}
