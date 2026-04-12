package com.quarkdown.rendering.html.post.document

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.rendering.html.post.resources.HTML_LIBRARY_OUTPUT_PATH
import com.quarkdown.rendering.html.post.resources.HTML_SCRIPT_FILE_NAME
import com.quarkdown.rendering.html.post.resources.HTML_SCRIPT_OUTPUT_PATH
import com.quarkdown.rendering.html.post.thirdparty.HeadContribution
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.InputType
import kotlinx.html.aside
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.kbd
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

/**
 * Builds a full HTML document wrapping the rendered content, replacing the old template-based approach.
 * Uses kotlinx.html DSL to programmatically construct the document structure.
 *
 * @param context the rendering context containing document metadata, attributes, and configuration
 * @param relativePathToRoot the relative path from the current document to the root directory,
 *                           used to correctly reference shared resources (scripts, themes, etc.)
 * @param sidebarContent the pre-rendered sidebar HTML content (table of contents)
 */
class HtmlDocumentBuilder(
    private val context: Context,
    private val relativePathToRoot: String,
    private val sidebarContent: CharSequence,
) {
    private val document = context.documentInfo

    private val pageFormats = document.layout.getPageFormatsWithDefault(document.type.defaultPageFormat)

    /**
     * Builds the full HTML document wrapping the given [content].
     * @param content the rendered body content to embed
     * @return the complete HTML document as a [CharSequence]
     */
    fun build(content: CharSequence): CharSequence =
        buildString {
            appendLine("<!DOCTYPE html>")
            appendHTML(prettyPrint = true, xhtmlCompatible = false).html {
                document.locale?.tag?.let { lang = it }

                head { buildHead() }
                body(classes = bodyClasses()) { buildBody(content) }
            }
        }

    private fun HEAD.buildHead() {
        documentMetadata()
        viewport()
        quarkdownMeta()
        title(document.name ?: "Quarkdown")
        quarkdownScript()
        thirdPartyLibraries()
        themeStylesheet()
        documentStyle()
        documentTypeInitScript()
        sidebarTemplate()
    }

    /** Emits generator, charset, and optional description/keywords/author meta tags. */
    private fun HEAD.documentMetadata() {
        meta(name = "generator", content = "Quarkdown")
        meta(charset = "UTF-8")

        document.description
            ?.let { meta(name = "description", content = it) }

        document.keywords
            .takeIf { it.isNotEmpty() }
            ?.joinToString()
            ?.let { meta(name = "keywords", content = it) }

        document.authors
            .takeIf { it.isNotEmpty() }
            ?.joinToString { it.name }
            ?.let { meta(name = "author", content = it) }
    }

    /** Emits the viewport meta tag, disabling user scaling for slides. */
    private fun HEAD.viewport() {
        val viewportContent =
            buildString {
                append("width=device-width, initial-scale=1.0")
                if (document.type == DocumentType.SLIDES) {
                    append(", maximum-scale=1.0, user-scalable=no")
                }
            }
        meta(name = "viewport", content = viewportContent)
    }

    /** Emits Quarkdown-specific meta tags for root path resolution and, for docs, the search index. */
    private fun HEAD.quarkdownMeta() {
        meta(name = "quarkdown:root-path", content = relativePathToRoot)
        if (document.type == DocumentType.DOCS) {
            meta(name = "quarkdown:search-index", content = "$relativePathToRoot/search-index.json")
        }
    }

    /** Loads the main Quarkdown script and initializes the capabilities object. */
    private fun HEAD.quarkdownScript() {
        script(src = "$relativePathToRoot/$HTML_SCRIPT_OUTPUT_PATH/$HTML_SCRIPT_FILE_NAME") {}
        script { unsafe { raw("const capabilities = window.quarkdownCapabilities") } }
    }

    /**
     * Emits `<script>` and `<link>` tags for all required third-party libraries.
     * Inclusion conditions and head contributions are defined in [ThirdPartyLibrary],
     * which serves as the single source of truth shared with [ThirdPartyPostRendererResource][com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource].
     */
    private fun HEAD.thirdPartyLibraries() {
        ThirdPartyLibrary
            .all()
            .filter { it.isRequired(context) }
            .forEach { library ->
                library.headContributions.forEach { contribution ->
                    when (contribution) {
                        is HeadContribution.Script -> {
                            script(src = "$relativePathToRoot/$HTML_LIBRARY_OUTPUT_PATH/${contribution.path}") {}
                        }

                        is HeadContribution.DeferredScript -> {
                            script(src = "$relativePathToRoot/$HTML_LIBRARY_OUTPUT_PATH/${contribution.path}") {
                                attributes["defer"] = "true"
                            }
                        }

                        is HeadContribution.Stylesheet -> {
                            link(
                                rel = "stylesheet",
                                href = "$relativePathToRoot/$HTML_LIBRARY_OUTPUT_PATH/${contribution.path}",
                            )
                        }

                        is HeadContribution.InlineScript -> {
                            script { unsafe { raw(contribution.content) } }
                        }

                        is HeadContribution.ContextualInlineScript -> {
                            script { unsafe { raw(contribution.contentProvider(context)) } }
                        }
                    }
                }
            }
    }

    private fun HEAD.themeStylesheet() {
        link(rel = "stylesheet", href = "$relativePathToRoot/theme/theme.css")
    }

    /** Embeds the document's CSS stylesheet, generated by [HtmlDocumentStylesheet]. */
    private fun HEAD.documentStyle() {
        style {
            unsafe { raw(HtmlDocumentStylesheet(context, pageFormats).build()) }
        }
    }

    /** Emits the script that instantiates the document type handler (e.g. `PlainDocument`, `SlidesDocument`). */
    private fun HEAD.documentTypeInitScript() {
        val docClassName =
            when (document.type) {
                DocumentType.PLAIN -> "PlainDocument"
                DocumentType.SLIDES -> "SlidesDocument"
                DocumentType.PAGED -> "PagedDocument"
                DocumentType.DOCS -> "DocsDocument"
            }
        script {
            unsafe {
                raw("prepare(new $docClassName());")
            }
        }
    }

    /**
     * Injects the sidebar (table of contents) wrapped in a `<template>` tag,
     * so the front-end script can clone and insert it at the appropriate location.
     */
    private fun HEAD.sidebarTemplate() {
        val navContent =
            createHTML().nav("sidebar") {
                attributes["role"] = "doc-toc"
                unsafe { raw(sidebarContent.toString()) }
            }
        unsafe {
            raw("<template id=\"sidebar-template\">")
            raw(navContent)
            raw("</template>")
        }
    }

    /** Builds the CSS class string for `<body>`, including document type and optional multicolumn flag. */
    private fun bodyClasses(): String =
        buildString {
            append("quarkdown quarkdown-")
            append(document.type.name.lowercase())
            if (pageFormats.any { it.columnCount != null }) {
                append(" multicolumn")
            }
        }

    /** Dispatches to the appropriate body builder for the current document type. */
    private fun BODY.buildBody(content: CharSequence) {
        when (document.type) {
            DocumentType.PLAIN -> plainBody(content)
            DocumentType.SLIDES -> slidesBody(content)
            DocumentType.PAGED -> pagedBody(content)
            DocumentType.DOCS -> docsBody(content)
        }
    }

    private fun BODY.plainBody(content: CharSequence) {
        aside("margin-area") { id = "margin-area-left" }
        main { unsafe { raw(content.toString()) } }
        aside("margin-area") { id = "margin-area-right" }
    }

    private fun BODY.slidesBody(content: CharSequence) {
        div("reveal") {
            div("slides") {
                unsafe { raw(content.toString()) }
            }
        }
    }

    private fun BODY.pagedBody(content: CharSequence) {
        div("paged-content-wrapper") {
            unsafe { raw(content.toString()) }
        }
    }

    private fun BODY.docsBody(content: CharSequence) {
        header {
            aside("margin-area")
            main {
                div("search-wrapper") {
                    div("search-field") {
                        i("bi bi-search")
                        input(type = InputType.text) {
                            id = "search-input"
                            placeholder = "Search"
                            attributes["autocomplete"] = "off"
                        }
                        kbd { +"/" }
                    }
                }
            }
            aside("margin-area")
        }
        div("content-wrapper") {
            aside("margin-area") {
                id = "margin-area-left"
                div("position-top")
                div("position-middle")
                div("position-bottom")
            }
            main {
                unsafe { raw(content.toString()) }
                div {
                    id = "footnote-area"
                    div("footnote-rule")
                }
                div { id = "sibling-pages-button-area" }
                footer {
                    div("position-left")
                    div("position-center")
                    div("position-right")
                }
            }
            aside("margin-area") {
                id = "margin-area-right"
                div("position-top")
                div("position-middle")
                div("position-bottom")
            }
        }
    }
}
