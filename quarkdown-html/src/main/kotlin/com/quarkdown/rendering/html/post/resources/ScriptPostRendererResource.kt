package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup

/**
 * A [PostRendererResource] that includes runtime JavaScript files required by the HTML output.
 *
 * The `quarkdown.js` script is pre-bundled from TypeScript sources and provides client-side functionality
 * for interactive Quarkdown features.
 */
class ScriptPostRendererResource : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        resources +=
            OutputResourceGroup(
                name = "script",
                resources = retrieveScriptComponentsArtifacts(),
            )
    }

    /**
     * @return a set that contains an output artifact for each required script component
     */
    private fun retrieveScriptComponentsArtifacts(): Set<OutputResource> =
        setOf(
            LazyOutputArtifact.internal(
                resource = "/render/script/quarkdown.js",
                // The name is not used here, as this artifact will be concatenated to others in generateResources.
                name = "quarkdown",
                type = ArtifactType.JAVASCRIPT,
            ),
        )
}
