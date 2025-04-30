package com.quarkdown.core.rendering

import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.template.TemplateProcessor

/**
 * Strategy used to run the post-rendering stage:
 * the rendered content from the rendering stage is injected into a template offered by the post-renderer.
 * Additionally, the post-renderer provides the output resources that can be saved to file.
 */
interface PostRenderer {
    /**
     * Rules that determine the default behavior of the media storage.
     * For example, HTML requires local media to be accessible from the file system,
     * hence it's preferred to copy local media to the output directory;
     * it's not necessary to store remote media locally.
     * On the other hand, for example, LaTeX rendering (not yet supported) would require
     * all media to be stored locally, as it does not support remote media.
     */
    val preferredMediaStorageOptions: MediaStorageOptions

    /**
     * Creates a new instance of a template processor for this rendering strategy.
     * A template adds static content to the output code, and supports injection of values via placeholder keys, like a template file.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
     * See `resources/render/html-wrapper.html.template` for an HTML template.
     * @return a new instance of the corresponding template processor
     * @see TemplateProcessor
     */
    fun createTemplateProcessor(): TemplateProcessor

    /**
     * Generates the required output resources.
     * Resources are abstractions of files that are generated during the rendering process and that can be saved on disk.
     * @param rendered the rendered content, output of the rendering stage
     * @return the generated output resources
     */
    fun generateResources(rendered: CharSequence): Set<OutputResource>

    /**
     * Accepts a post-renderer visitor.
     * @param visitor visitor to accept
     * @return the result of the visit operation
     */
    fun <T> accept(visitor: PostRendererVisitor<T>): T
}

/**
 * Wraps rendered code in a template.
 * @param content code to wrap
 * @return [content], wrapped in the corresponding template for this rendering strategy
 * @see TemplateProcessor
 */
fun PostRenderer.wrap(content: CharSequence) = createTemplateProcessor().content(content).process()
