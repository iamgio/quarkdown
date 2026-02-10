package com.quarkdown.rendering.html.post.document

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.util.Escape
import com.quarkdown.core.util.get
import com.quarkdown.core.util.withDefault
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

    private val pageFormat = document.layout.pageFormat.withDefault(document.type.defaultPageFormat)

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
        pagedScripts()
        slidesScripts()
        iconLibrary()
        themeStylesheet()
        codeScripts()
        mathScripts()
        mermaidScripts()
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
        script(src = "$relativePathToRoot/script/quarkdown.js") {}
        script { unsafe { raw("const capabilities = window.quarkdownCapabilities") } }
    }

    /** Loads the Paged.js polyfill for paged documents. No-op for other document types. */
    private fun HEAD.pagedScripts() {
        if (document.type != DocumentType.PAGED) return
        script { unsafe { raw("window.PagedConfig = {auto: false};") } }
        script(src = "https://unpkg.com/pagedjs@0.4.3/dist/paged.polyfill.js") {}
    }

    /** Loads Reveal.js scripts and styles for slide documents. No-op for other document types. */
    private fun HEAD.slidesScripts() {
        if (document.type != DocumentType.SLIDES) return
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.2.1/reveal.js") {}
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.2.1/plugin/notes/notes.js") {}
        link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.2.1/reset.css")
        link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.2.1/reveal.css")
        link(rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.2.1/theme/white.css")
    }

    private fun HEAD.iconLibrary() {
        link(
            rel = "stylesheet",
            href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css",
        )
    }

    private fun HEAD.themeStylesheet() {
        link(rel = "stylesheet", href = "$relativePathToRoot/theme/theme.css")
    }

    /** Loads Highlight.js and its plugins for code highlighting. No-op if the document contains no code. */
    private fun HEAD.codeScripts() {
        if (!context.attributes.hasCode) return
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js") {}
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/highlightjs-line-numbers.js/2.9.0/highlightjs-line-numbers.min.js") {}
        script(src = "https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.js") {}
        link(rel = "stylesheet", href = "https://unpkg.com/highlightjs-copy/dist/highlightjs-copy.min.css")
        script { unsafe { raw("capabilities.code = true;") } }
    }

    /** Loads KaTeX and emits user-defined TeX macros. No-op if the document contains no math. */
    private fun HEAD.mathScripts() {
        if (!context.attributes.hasMath) return
        link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/katex@0.16.22/dist/katex.min.css") {
            attributes["integrity"] = "sha384-5TcZemv2l/9On385z///+d7MSYlvIEw9FuZTIdZ14vJLqWphw7e7ZPuOiCHJcFCP"
            attributes["crossorigin"] = "anonymous"
        }
        script(src = "https://cdn.jsdelivr.net/npm/katex@0.16.22/dist/katex.min.js") {
            attributes["defer"] = "true"
            attributes["integrity"] = "sha384-cMkvdD8LoxVzGF/RPUKAcvmm49FQ0oxwDF3BGKtDXcEc+T1b2N+teh/OJfpU0jr6"
            attributes["crossorigin"] = "anonymous"
        }
        script {
            unsafe {
                raw(
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
                    },
                )
            }
        }
    }

    /** Loads the Mermaid library for diagram rendering. No-op if the document contains no diagrams. */
    private fun HEAD.mermaidScripts() {
        if (!context.attributes.hasMermaidDiagram) return
        script(src = "https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js") {}
        script { unsafe { raw("capabilities.mermaid = true;") } }
    }

    /** Embeds the document's CSS stylesheet, generated by [HtmlDocumentStylesheet]. */
    private fun HEAD.documentStyle() {
        style {
            unsafe { raw(HtmlDocumentStylesheet(context).build()) }
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
            if (pageFormat.get { columnCount } != null) {
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
        unsafe { raw(content.toString()) }
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
