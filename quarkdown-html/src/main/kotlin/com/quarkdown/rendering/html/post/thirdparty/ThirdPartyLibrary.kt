package com.quarkdown.rendering.html.post.thirdparty

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.util.Escape

/**
 * Single source of truth for a third-party library bundled in the HTML output.
 * @param names the library directory name under `lib/` in the output
 * @param headContributions the HTML `<head>` elements this library requires
 * @see com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource
 * @see com.quarkdown.rendering.html.post.document.HtmlDocumentBuilder
 */
sealed class ThirdPartyLibrary(
    val names: List<String>,
    val headContributions: List<HeadContribution> = emptyList(),
) {
    /**
     * Whether this library should be included in the output for the given rendering [context].
     */
    abstract fun isRequired(context: Context): Boolean

    /**
     * Bootstrap Icons: icon font used throughout the UI. Always included.
     */
    data object BootstrapIcons : ThirdPartyLibrary(
        names = listOf("bootstrap-icons"),
        headContributions = listOf(HeadContribution.Stylesheet("bootstrap-icons/bootstrap-icons.min.css")),
    ) {
        override fun isRequired(context: Context) = true
    }

    /**
     * Highlight.js and its plugins for syntax highlighting of code blocks.
     * The hljs color theme CSS is inlined into color themes at SCSS compile time,
     * so no theme stylesheet is emitted here.
     */
    data object HighlightJs : ThirdPartyLibrary(
        names = listOf("highlight.js", "highlightjs-line-numbers", "highlightjs-copy"),
        headContributions =
            listOf(
                HeadContribution.Script("highlight.js/highlightjs.min.js"),
                HeadContribution.Script("highlightjs-line-numbers/highlightjs-line-numbers.min.js"),
                HeadContribution.Script("highlightjs-copy/highlightjs-copy.min.js"),
                HeadContribution.Stylesheet("highlightjs-copy/highlightjs-copy.min.css"),
                HeadContribution.InlineScript("capabilities.code = true;"),
            ),
    ) {
        override fun isRequired(context: Context) = context.attributes.hasCode
    }

    /**
     * KaTeX for rendering math expressions.
     * Emits user-defined TeX macros as a contextual inline script.
     */
    data object KaTeX : ThirdPartyLibrary(
        names = listOf("katex"),
        headContributions =
            listOf(
                HeadContribution.Stylesheet("katex/katex.min.css"),
                HeadContribution.DeferredScript("katex/katex.min.js"),
                HeadContribution.ContextualInlineScript { context ->
                    buildString {
                        appendLine("capabilities.math = true;")
                        appendLine()
                        append("window.texMacros = {")
                        context.documentInfo.tex.macros.forEach { (key, value) ->
                            append('"')
                            append(Escape.JavaScript.escape(key))
                            append("\": \"")
                            append(Escape.JavaScript.escape(value))
                            append("\",")
                        }
                        append("}")
                    }
                },
            ),
    ) {
        override fun isRequired(context: Context) = context.attributes.hasMath
    }

    /** Mermaid for rendering diagrams. */
    data object Mermaid : ThirdPartyLibrary(
        names = listOf("mermaid"),
        headContributions =
            listOf(
                HeadContribution.Script("mermaid/mermaid.min.js"),
                HeadContribution.InlineScript("capabilities.mermaid = true;"),
            ),
    ) {
        override fun isRequired(context: Context) = context.attributes.hasMermaidDiagram
    }

    /** Reveal.js for slide-based presentations. */
    data object RevealJs : ThirdPartyLibrary(
        names = listOf("reveal.js"),
        headContributions =
            listOf(
                HeadContribution.Script("reveal.js/reveal.js"),
                HeadContribution.Script("reveal.js/plugin/notes.js"),
                HeadContribution.Stylesheet("reveal.js/reset.css"),
                HeadContribution.Stylesheet("reveal.js/reveal.css"),
                HeadContribution.Stylesheet("reveal.js/theme/white.css"),
            ),
    ) {
        override fun isRequired(context: Context) = context.documentInfo.type == DocumentType.SLIDES
    }

    /** Paged.js polyfill for paged (book/article) documents. */
    data object PagedJs : ThirdPartyLibrary(
        names = listOf("pagedjs"),
        headContributions =
            listOf(
                HeadContribution.InlineScript("window.PagedConfig = {auto: false};"),
                HeadContribution.Script("pagedjs/paged.polyfill.js"),
            ),
    ) {
        override fun isRequired(context: Context) = context.documentInfo.type == DocumentType.PAGED
    }

    companion object {
        /** All third-party libraries that may be included in the output. */
        fun all(): List<ThirdPartyLibrary> =
            listOf(
                BootstrapIcons,
                HighlightJs,
                KaTeX,
                Mermaid,
                RevealJs,
                PagedJs,
            )
    }
}
