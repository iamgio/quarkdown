package com.quarkdown.core.pipeline

import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.then
import com.quarkdown.core.pipeline.stage.thenOptionally
import com.quarkdown.core.pipeline.stages.AttachmentStage
import com.quarkdown.core.pipeline.stages.AttributesUpdateStage
import com.quarkdown.core.pipeline.stages.FunctionCallExpansionStage
import com.quarkdown.core.pipeline.stages.LexingStage
import com.quarkdown.core.pipeline.stages.LibrariesRegistrationStage
import com.quarkdown.core.pipeline.stages.ParsingStage
import com.quarkdown.core.pipeline.stages.PostRenderingStage
import com.quarkdown.core.pipeline.stages.RenderingStage
import com.quarkdown.core.pipeline.stages.ResourceGenerationStage
import com.quarkdown.core.pipeline.stages.TreeTraversalStage
import com.quarkdown.core.rendering.RenderingComponents

/**
 * Factory for creating standard pipeline stage chains.
 */
object PipelineChainFactory {
    /**
     * Creates a full pipeline stage chain that processes the input source text
     * through all stages, up to resource generation.
     *
     * @param source the raw input text to be processed
     * @param renderingComponents the rendering components to use in the rendering stages
     * @return a pipeline stage that processes the input source text and produces output resources
     */
    fun fullChain(
        source: CharSequence,
        renderingComponents: RenderingComponents,
        options: PipelineOptions,
    ): PipelineStage<Unit, Set<OutputResource>> =
        AttachmentStage then
            LibrariesRegistrationStage then
            LexingStage(source) then
            ParsingStage then
            AttributesUpdateStage(preferredMediaStorageOptions = renderingComponents.postRenderer.preferredMediaStorageOptions) then
            FunctionCallExpansionStage then
            TreeTraversalStage then
            RenderingStage(renderingComponents.nodeRenderer) thenOptionally
            PostRenderingStage(renderingComponents.postRenderer).takeIf { options.wrapOutput } then
            ResourceGenerationStage(renderingComponents.postRenderer)
}
