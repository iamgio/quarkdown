package com.quarkdown.rendering.html.post.thirdparty

import com.quarkdown.core.context.Context

/**
 * A contribution to the HTML `<head>` section emitted by a [ThirdPartyLibrary].
 * Each variant describes a specific type of HTML element to generate.
 */
sealed interface HeadContribution {
    /**
     * A `<script src="...">` tag loading an external script file.
     * @param path file path relative to the library's output directory
     */
    data class Script(
        val path: String,
    ) : HeadContribution

    /**
     * A `<script src="..." defer>` tag loading an external script file with deferred execution.
     * @param path file path relative to the library's output directory
     */
    data class DeferredScript(
        val path: String,
    ) : HeadContribution

    /**
     * A `<link rel="stylesheet" href="...">` tag loading an external CSS file.
     * @param path file path relative to the library's output directory
     */
    data class Stylesheet(
        val path: String,
    ) : HeadContribution

    /**
     * A `<script>` tag with inline JavaScript content.
     * @param content the raw JavaScript source to embed
     */
    data class InlineScript(
        val content: String,
    ) : HeadContribution

    /**
     * A `<script>` tag whose inline content depends on the rendering context.
     * @param contentProvider a function producing the JavaScript source from the current [Context]
     */
    data class ContextualInlineScript(
        val contentProvider: (Context) -> String,
    ) : HeadContribution
}
