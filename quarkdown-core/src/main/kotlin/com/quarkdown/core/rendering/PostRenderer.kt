package com.quarkdown.core.rendering

import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.pipeline.output.OutputResource

/**
 * Strategy used to run the post-rendering stage:
 * the rendered content from the rendering stage is wrapped in the document structure offered by the post-renderer.
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
     * Wraps rendered content in the full document structure for this rendering strategy.
     * For example, an HTML post-renderer wraps content in `<html><head>...</head><body>...</body></html>`.
     * @param content the rendered content to wrap
     * @return the wrapped content
     */
    fun wrap(content: CharSequence): CharSequence

    /**
     * Generates the required output resources.
     * Resources are abstractions of files that are generated during the rendering process and that can be saved on disk.
     * @param rendered the rendered content, output of the rendering stage
     * @return the generated output resources
     */
    fun generateResources(rendered: CharSequence): Set<OutputResource>

    /**
     * Given the output [resources] produced by [generateResources], merges them into a single resource
     * which complies with [com.quarkdown.core.pipeline.Pipeline.execute]'s output type.
     *
     * Wrapping can happen by:
     * - Grouping the resources into an [com.quarkdown.core.pipeline.output.OutputResourceGroup] (e.g. HTML output).
     * - Selecting a single resource from the set (e.g. PDF output).
     */
    fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ): OutputResource?
}
