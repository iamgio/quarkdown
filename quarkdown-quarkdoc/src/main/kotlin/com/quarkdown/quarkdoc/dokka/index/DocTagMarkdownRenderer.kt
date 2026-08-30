package com.quarkdown.quarkdoc.dokka.index

import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.B
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.H1
import org.jetbrains.dokka.model.doc.H2
import org.jetbrains.dokka.model.doc.H3
import org.jetbrains.dokka.model.doc.H4
import org.jetbrains.dokka.model.doc.H5
import org.jetbrains.dokka.model.doc.H6
import org.jetbrains.dokka.model.doc.I
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Ol
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.U
import org.jetbrains.dokka.model.doc.Ul

/**
 * Renderer of KDoc [DocTag] trees to Markdown, used to pre-extract documentation
 * into the docs index directly from the Dokka model.
 */
object DocTagMarkdownRenderer {
    private val HEADING_DEPTHS =
        mapOf(
            H1::class to 1,
            H2::class to 2,
            H3::class to 3,
            H4::class to 4,
            H5::class to 5,
            H6::class to 6,
        )

    /**
     * @param tags the documentation tags to render
     * @return the Markdown representation of [tags]
     */
    fun render(tags: List<DocTag>): String = renderBlocks(tags).trim()

    /**
     * Renders [tags] as Markdown blocks separated by blank lines:
     * each block-level tag forms its own block,
     * and each run of consecutive inline tags forms an implicit paragraph.
     */
    private fun renderBlocks(tags: List<DocTag>): String =
        tags
            .chunkedByBlocks()
            .mapNotNull(::renderChunk)
            .joinToString(separator = "\n\n")

    /**
     * @return [this] tags split into chunks: each block tag on its own,
     *         and consecutive inline tags grouped together
     */
    private fun List<DocTag>.chunkedByBlocks(): List<List<DocTag>> {
        val chunks = mutableListOf<MutableList<DocTag>>()
        forEach { tag ->
            val inlineRun = chunks.lastOrNull()?.takeIf { isInline(it.first()) && isInline(tag) }
            inlineRun?.add(tag) ?: chunks.add(mutableListOf(tag))
        }
        return chunks
    }

    private fun renderChunk(chunk: List<DocTag>): String? =
        when {
            isInline(chunk.first()) -> {
                chunk
                    .joinToString(separator = "", transform = ::renderInline)
                    .collapseSpaces()
                    .takeIf(String::isNotBlank)
            }

            else -> {
                renderBlock(chunk.single())
            }
        }

    private fun isInline(tag: DocTag): Boolean =
        when (tag) {
            is Text, is CodeInline, is B, is I, is U, is Br -> true
            is A, is DocumentationLink -> tag.children.all(::isInline)
            else -> false
        }

    /**
     * @return the Markdown block for [tag], or `null` if it has no renderable content
     */
    private fun renderBlock(tag: DocTag): String? {
        HEADING_DEPTHS[tag::class]?.let { depth ->
            return "#".repeat(depth) + " " + renderInlineChildren(tag).collapseSpaces()
        }
        return when (tag) {
            is CodeBlock -> renderCodeBlock(tag)
            is Ul -> renderList(tag, ordered = false)
            is Ol -> renderList(tag, ordered = true)
            else -> renderBlocks(tag.children).takeIf(String::isNotBlank)
        }
    }

    private fun renderCodeBlock(tag: CodeBlock): String {
        val language = tag.params["lang"].orEmpty()
        return "```$language\n" + plainText(tag).trim() + "\n```"
    }

    /**
     * Renders a list, indenting each item's continuation lines to stay within the item.
     */
    private fun renderList(
        tag: DocTag,
        ordered: Boolean,
    ): String =
        tag.children
            .filterIsInstance<Li>()
            .mapIndexedNotNull { index, item ->
                val marker = if (ordered) "${index + 1}. " else "* "
                renderBlocks(item.children)
                    .ifBlank { return@mapIndexedNotNull null }
                    .withFirstLinePrefix(marker)
            }.joinToString(separator = "\n")

    /**
     * @return [this] multiline content prefixed with [marker] on its first line,
     *         with the following lines indented to align under it
     */
    private fun String.withFirstLinePrefix(marker: String): String =
        lines()
            .mapIndexed { index, line ->
                when {
                    index == 0 -> marker + line
                    line.isBlank() -> ""
                    else -> " ".repeat(marker.length) + line
                }
            }.joinToString(separator = "\n")

    /**
     * @return the Markdown representation of an inline [tag]
     */
    private fun renderInline(tag: DocTag): String =
        when (tag) {
            is Text -> tag.body
            is Br -> " "
            is CodeInline -> "`${plainText(tag)}`"
            is B, is U -> renderInlineChildren(tag).collapseSpaces().wrapIfNotBlank("**")
            is I -> renderInlineChildren(tag).collapseSpaces().wrapIfNotBlank("*")
            is A -> renderLink(tag)
            else -> renderInlineChildren(tag)
        }

    private fun renderLink(tag: A): String {
        val href = tag.params["href"].orEmpty()
        val label = renderInlineChildren(tag).collapseSpaces()
        return if (href.isBlank() || label.isBlank()) label else "[$label]($href)"
    }

    private fun renderInlineChildren(tag: DocTag): String = tag.children.joinToString(separator = "", transform = ::renderInline)

    /**
     * @return the concatenated raw text of [tag]'s subtree, with line breaks preserved
     */
    private fun plainText(tag: DocTag): String =
        when (tag) {
            is Text -> tag.body
            is Br -> "\n"
            else -> tag.children.joinToString(separator = "", transform = ::plainText)
        }

    private fun String.collapseSpaces(): String = replace(Regex("\\s+"), " ").trim()

    private fun String.wrapIfNotBlank(delimiter: String): String = if (isBlank()) this else "$delimiter$this$delimiter"
}
