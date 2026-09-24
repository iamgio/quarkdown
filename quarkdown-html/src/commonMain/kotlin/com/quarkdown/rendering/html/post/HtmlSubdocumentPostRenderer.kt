package com.quarkdown.rendering.html.post

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.rendering.html.post.resources.MediaPostRendererResource

/**
 * A [PostRenderer] for subdocuments, which generates a group (directory) containing a `index.html` resource,
 * and a media directory if needed, and redirects scripts, themes, and other resources to the root's ([HtmlPostRenderer]) resources.
 *
 * @param base the base [HtmlPostRenderer] to delegate rendering to
 */
class HtmlSubdocumentPostRenderer(
    private val base: HtmlPostRenderer,
) : PostRenderer by base {
    /**
     * Note: Quarkdown currently exports subdocuments at a single level of nesting, hence the relative path to root is hardcoded to `..`.
     * This may change in the future.
     */
    constructor(context: Context) : this(
        HtmlPostRenderer(
            context,
            relativePathToRoot = "..",
            resourcesProvider = { setOf(MediaPostRendererResource(context.mediaStorage)) },
        ),
    )

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        setOf(
            OutputResourceGroup(
                name = base.context.subdocument.getOutputFileName(base.context),
                resources = base.generateResources(rendered),
            ),
        )
}
