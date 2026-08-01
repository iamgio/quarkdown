package com.quarkdown.rendering.markdown.post

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.copy
import com.quarkdown.core.rendering.PostRenderer

/**
 * Post-renderer that generates GitHub Flavored Markdown output artifacts.
 *
 * - Produces a single Markdown file if there is only one subdocument.
 * - Produces a resource group of Markdown files if there are multiple subdocuments.
 */
class GfmPostRenderer(
    private val context: Context,
) : PostRenderer {
    override val preferredMediaStorageOptions: MediaStorageOptions =
        ReadOnlyMediaStorageOptions(enableLocalMediaStorage = true)

    override fun wrap(content: CharSequence): CharSequence = content

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            this +=
                TextOutputArtifact(
                    name = context.subdocument.getOutputFileName(context),
                    content = rendered.trimEnd(),
                    type = ArtifactType.MARKDOWN,
                )

            if (!context.mediaStorage.isEmpty) {
                this += context.mediaStorage.toResource()
            }
        }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ): OutputResource {
        // Single output file.
        resources.singleOrNull()?.let {
            return it.copy(name = name)
        }
        // Multiple output files.
        return OutputResourceGroup(
            name = name,
            resources = resources,
        )
    }
}
