package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument

private const val LLMS_TXT_FILE_NAME = "llms.txt"

// Base name of the root document's Markdown output, mirroring GfmPostRenderer's flat naming.
private const val ROOT_MARKDOWN_NAME = "index"
private const val MARKDOWN_EXTENSION = ".md"

/**
 * A [PostRendererResource] that generates an `llms.txt` at the site root: a curated,
 * agent-friendly index of every page in the site as absolute links to their Markdown counterparts.
 *
 * The generated file follows the [llmstxt.org](https://llmstxt.org) convention:
 * - an H1 with the document title,
 * - a blockquote with the description supplied by `.llmstxt`,
 * - a `## Docs` section listing the root document and every subdocument
 *   as a Markdown link (if available, HTML otherwise).
 *
 * Requires:
 * - [com.quarkdown.core.context.options.HtmlOptions.llmsTxtContent] to be set via `.llmstxt`.
 * - [com.quarkdown.core.context.options.HtmlOptions.baseUrl] to be set via `.htmloptions`.
 *
 * @param context the context of the document being rendered
 */
class LlmsTxtPostRendererResource(
    context: Context,
) : SubdocumentMapperPostRendererResource(context) {
    override val runsInPreviewMode: Boolean = false

    override val resourceName: String
        get() = LLMS_TXT_FILE_NAME

    override fun buildResourceContent(subdocuments: Sequence<Pair<Subdocument, Context>>): String? {
        // Skip entirely when the site hasn't opted into llms.txt via `.llmstxt`.
        val description = context.options.html.llmsTxtContent ?: return null
        val title = context.documentInfo.name
        val isMarkdownMirrorAvailable = context.options.html.isMarkdownMirrorAvailable
        val outExtension = MARKDOWN_EXTENSION.takeIf { isMarkdownMirrorAvailable }

        return buildString {
            title?.let {
                appendLine("# $it")
                appendLine()
            }
            description.lineSequence().forEach { appendLine("> $it") }
            appendLine()
            appendLine("## Docs")
            appendLine()

            // Root document.
            val rootLabel = title ?: Subdocument.Root.name
            val rootUrl = if (isMarkdownMirrorAvailable) getSubdocumentUrl(ROOT_MARKDOWN_NAME, MARKDOWN_EXTENSION) else baseUrl
            appendMarkdownLink(label = rootLabel, url = rootUrl)

            // Subdocuments.
            subdocuments
                .map { (subdocument, subdocumentContext) ->
                    val label = subdocumentContext.documentInfo.name ?: subdocument.name
                    label to super.getSubdocumentUrl(subdocument, subdocumentContext, outExtension)
                }.sortedBy { (label, _) -> label.lowercase() }
                .forEach { (label, url) -> appendMarkdownLink(label = label, url = url) }
        }
    }

    private fun StringBuilder.appendMarkdownLink(
        label: String,
        url: String,
    ) {
        append("- [")
        append(label)
        append("](")
        append(url)
        append(")")
        appendLine()
    }
}
