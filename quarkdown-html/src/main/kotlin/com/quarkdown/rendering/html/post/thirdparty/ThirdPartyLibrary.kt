package com.quarkdown.rendering.html.post.thirdparty

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.util.Escape
import com.quarkdown.rendering.html.post.resources.ThirdPartyResourceLoader

/**
 * Single source of truth for a third-party library bundled in the HTML output.
 * Each subclass defines:
 * - The library [name] (matching the key in `third-party-manifest.json`)
 * - When the library [isRequired] based on the document's context
 * - What HTML [headContributions] (`<script>`, `<link>`, inline scripts) it needs in `<head>`
 *
 * Both [com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource]
 * (which copies files to the output) and
 * [com.quarkdown.rendering.html.post.document.HtmlDocumentBuilder]
 * (which emits `<script>`/`<link>` tags) use this hierarchy, eliminating duplicated conditions.
 *
 * @param name the library directory name under `lib/` in the output, matching the manifest key
 * @param headContributions the HTML `<head>` elements this library requires
 */
sealed class ThirdPartyLibrary(
    val name: String,
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
        name = "bootstrap-icons",
        headContributions = listOf(HeadContribution.Stylesheet("bootstrap-icons.min.css")),
    ) {
        override fun isRequired(context: Context) = true
    }

    /**
     * Highlight.js and its plugins for syntax highlighting of code blocks.
     * The hljs color theme CSS is inlined into color themes at SCSS compile time,
     * so no theme stylesheet is emitted here.
     */
    data object HighlightJs : ThirdPartyLibrary(
        name = "highlight.js",
        headContributions =
            listOf(
                HeadContribution.Script("highlight.min.js"),
                HeadContribution.Script("highlightjs-line-numbers.min.js"),
                HeadContribution.Script("highlightjs-copy.min.js"),
                HeadContribution.Stylesheet("highlightjs-copy.min.css"),
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
        name = "katex",
        headContributions =
            listOf(
                HeadContribution.Stylesheet("katex.min.css"),
                HeadContribution.DeferredScript("katex.min.js"),
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
        name = "mermaid",
        headContributions =
            listOf(
                HeadContribution.Script("mermaid.min.js"),
                HeadContribution.InlineScript("capabilities.mermaid = true;"),
            ),
    ) {
        override fun isRequired(context: Context) = context.attributes.hasMermaidDiagram
    }

    /** Reveal.js for slide-based presentations. */
    data object RevealJs : ThirdPartyLibrary(
        name = "reveal.js",
        headContributions =
            listOf(
                HeadContribution.Script("reveal.js"),
                HeadContribution.Script("plugin/notes.js"),
                HeadContribution.Stylesheet("reset.css"),
                HeadContribution.Stylesheet("reveal.css"),
                HeadContribution.Stylesheet("theme/white.css"),
            ),
    ) {
        override fun isRequired(context: Context) = context.documentInfo.type == DocumentType.SLIDES
    }

    /** Paged.js polyfill for paged (book/article) documents. */
    data object PagedJs : ThirdPartyLibrary(
        name = "pagedjs",
        headContributions =
            listOf(
                HeadContribution.InlineScript("window.PagedConfig = {auto: false};"),
                HeadContribution.Script("paged.polyfill.js"),
            ),
    ) {
        override fun isRequired(context: Context) = context.documentInfo.type == DocumentType.PAGED
    }

    /**
     * Layout-specific font files (e.g. `fonts/latex`, `fonts/minimal`, `fonts/beamer`).
     * The library name is derived dynamically from [DocumentTheme.layout].
     * No head contributions are needed because fonts are referenced via SCSS `@import url()`.
     *
     * Inclusion is determined by checking whether the manifest actually contains
     * a `fonts/{layout}` entry, avoiding hardcoded theme-to-library mappings.
     */
    class LayoutFonts(
        theme: DocumentTheme,
    ) : ThirdPartyLibrary(
            name = "fonts/${theme.layout}",
        ) {
        override fun isRequired(context: Context) = ThirdPartyResourceLoader.contains(name)
    }

    companion object {
        /**
         * All third-party libraries that may be included in the output.
         * @param theme the active document theme, used to resolve font libraries
         */
        fun all(theme: DocumentTheme): List<ThirdPartyLibrary> =
            listOf(
                BootstrapIcons,
                HighlightJs,
                KaTeX,
                Mermaid,
                RevealJs,
                PagedJs,
                LayoutFonts(theme),
            )
    }
}
