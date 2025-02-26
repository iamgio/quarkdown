package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.media.storage.options.MediaStorageOptions
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.template.TemplateProcessor

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
     * Creates a new instance of a code wrapper for this rendering strategy.
     * A wrapper adds static content to the output code, and supports injection of values via placeholder keys, like a template file.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
     * See `resources/render` for templates.
     * @return a new instance of the corresponding wrapper
     */
    fun createCodeWrapper(): TemplateProcessor

    /**
     * Generates the required output resources.
     * Resources are abstractions of files that are generated during the rendering process and that can be saved on disk.
     * @param rendered the rendered content, output of the rendering stage
     * @return the generated output resources
     */
    fun generateResources(rendered: CharSequence): Set<OutputResource>
}

/**
 * Wraps rendered code in a template.
 * @param content code to wrap
 * @return [content], wrapped in the corresponding template for this rendering strategy
 * @see TemplateProcessor
 */
fun PostRenderer.wrap(content: CharSequence) = createCodeWrapper().content(content).build()
