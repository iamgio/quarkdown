package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.rendering.html.search.SearchIndex
import kotlinx.serialization.json.Json

/**
 * A [PostRendererResource] that outputs a search index as a JSON file.
 *
 * The generated `search-index.json` file is placed in the output directory
 * and can be fetched by client-side JavaScript to provide documentation search
 * without requiring a server.
 *
 * @param index the search index to serialize
 * @see com.quarkdown.rendering.html.search.SearchIndex
 * @see com.quarkdown.rendering.html.search.SearchIndexGenerator
 */
class SearchIndexPostRendererResource(
    private val index: SearchIndex,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        resources +=
            TextOutputArtifact(
                name = "search-index",
                content = Json.encodeToString(index),
                type = ArtifactType.JSON,
            )
    }
}
