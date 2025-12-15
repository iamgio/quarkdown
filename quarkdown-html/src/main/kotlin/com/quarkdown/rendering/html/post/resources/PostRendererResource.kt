package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.OutputResource

/**
 * Strategy that determines what [OutputResource]s are exported as part of the HTML output.
 *
 * Implementations of this interface are responsible for collecting and adding
 * specific types of resources (e.g., themes, scripts, media) to the output.
 *
 * @see com.quarkdown.rendering.html.post.HtmlPostRenderer
 */
sealed interface PostRendererResource {
    /**
     * Collects resources and adds them to the output set.
     * @param resources the mutable set to add resources to
     * @param rendered the rendered HTML content, which may be inspected to determine required resources
     */
    fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    )
}
