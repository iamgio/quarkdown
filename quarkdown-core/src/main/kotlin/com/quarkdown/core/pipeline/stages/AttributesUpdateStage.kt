package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.pipeline.stage.PeekPipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for updating attributes in the AST and context.
 *
 * - Sets the root of the AST
 * - Registers the current subdocument in the subdocument graph
 * - Updates the context's [MediaStorageOptions].
 */
class AttributesUpdateStage(
    private val preferredMediaStorageOptions: MediaStorageOptions,
) : PeekPipelineStage<AstRoot> {
    override val hook = null

    override fun peek(
        input: AstRoot,
        data: SharedPipelineData,
    ) {
        val context = data.context

        context.attributes.root = input
        context.subdocumentGraph = context.subdocumentGraph.addVertex(context.subdocument)

        // The chosen renderer has its own preferred media storage options.
        // For example, HTML requires local media to be accessible from the file system,
        // hence local files must be stored and copied to the output directory.
        // It does not require remote media to be stored, as they are linked to from the web.
        // On the other hand, for example, LaTeX rendering (not yet supported) would require
        // all media to be stored locally, as it does not support remote media.
        //
        // The options are merged: if a rule is already set by the user, it is not overridden.
        // These options must be set before traversing the tree, as media is stored during it.
        context.options.mergeMediaStorageOptions(this.preferredMediaStorageOptions)

        // The user can further force override the media storage options.
        context.options.mergeMediaStorageOptions(data.pipeline.options.mediaStorageOptionsOverrides)
    }
}
