package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryRegistrant
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for registering libraries with the document context.
 *
 * This stage takes a boolean as input (indicating whether to register libraries)
 * and produces a Set of [Library] objects as output. If the input is true, it
 * registers all libraries from the pipeline with the context.
 *
 * If the input is `false` (see [AttachmentStage]), it means the context is a scope context
 * and libraries should not be registered again, since they are inherited from the parent context.
 *
 * Libraries provide functions, extensions, and other capabilities to the document
 * processing system. They need to be registered with the context so that their
 * functions can be called from within the document.
 */
object LibrariesRegistrationStage : PipelineStage<Boolean, Set<Library>> {
    override val hook = PipelineHooks::afterRegisteringLibraries

    override fun process(
        input: Boolean,
        data: SharedPipelineData,
    ): Set<Library> {
        if (input) {
            LibraryRegistrant(data.context).registerAll(data.pipeline.libraries)
        }
        return data.pipeline.libraries
    }

    override fun invokeHook(
        data: SharedPipelineData,
        input: Boolean,
        output: Set<Library>,
    ) {
        if (input) {
            super.invokeHook(data, true, output)
        }
    }
}
