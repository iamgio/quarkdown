package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.pipeline.Pipelines
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for attaching a pipeline to the document context.
 *
 * This stage takes no specific input (Unit) and produces a Boolean output indicating
 * whether the attachment processing was successfully created if it was missing.
 *
 * In case the context is a scope context (e.g. for subdocuments),
 * the pipeline will likely already be attached from the parent context.
 * In this case, the output will be false, and hooks won't be invoked.
 */
object AttachmentStage : PipelineStage<Unit, Boolean> {
    override val hook = null

    override fun process(
        input: Unit,
        data: SharedPipelineData,
    ): Boolean {
        val uninitialized = data.context.attachedPipeline == null
        Pipelines.attach(data.context, data.pipeline)
        return uninitialized
    }
}
