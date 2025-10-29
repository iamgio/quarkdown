package com.quarkdown.core.pipeline.stage

import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineHooks

/**
 * Represents a stage in the document processing pipeline.
 *
 * Each pipeline stage takes an input of type [I], processes it, and produces an output of type [O].
 * Pipeline stages can be chained together to form a complete processing pipeline.
 *
 * Pipeline stages can also define hooks that are invoked after the stage completes processing,
 * allowing for custom behavior to be executed at specific points in the pipeline.
 */
interface PipelineStage<I, O> {
    /**
     * A hook function that is invoked after the stage completes processing.
     *
     * The hook is a function that takes a [PipelineHooks] object and returns a function,
     * which takes a [Pipeline] and the output [O] of this stage. This allows for custom
     * behavior to be executed at specific points in the pipeline.
     *
     * If `null`, no hook will be invoked after this stage.
     */
    val hook: ((PipelineHooks) -> Pipeline.(O) -> Unit)?

    /**
     * Processes the input [I] and produces an output [O].
     *
     * This is the main method that implements the stage's processing logic.
     *
     * @param input the input to process
     * @param data shared data that is passed between pipeline stages
     * @return the processed output
     */
    fun process(
        input: I,
        data: SharedPipelineData,
    ): O

    /**
     * Executes this pipeline stage with the given input and shared data.
     *
     * This method calls the [process] method to process the input and produce an output,
     * then invokes the hook function (if one is defined) using [invokeHook].
     *
     * This is the main entry point for executing a pipeline stage.
     *
     * @param input the input to process
     * @param data shared data that is passed between pipeline stages
     * @return the processed output
     */
    fun execute(
        input: I,
        data: SharedPipelineData,
    ): O =
        process(input, data).also {
            invokeHook(data, input, it)
        }

    /**
     * Invokes the hook function for this stage, if one is defined.
     *
     * This method is called after the stage's [process] method completes. It invokes
     * the hook function with the pipeline's hooks and the output of the stage.
     *
     * The hook is invoked for both the pipeline's hooks and the hooks of all registered libraries.
     *
     * @param data shared data that is passed between pipeline stages
     * @param input the input that was processed
     * @param output the output that was produced
     */
    fun invokeHook(
        data: SharedPipelineData,
        input: I,
        output: O,
    ) {
        val pipeline = data.pipeline

        fun invokeHook(hooks: PipelineHooks) {
            hook?.invoke(hooks)?.invoke(pipeline, output)
        }

        // Invoke the hook of this pipeline.
        pipeline.hooks?.let(::invokeHook)
        // Invoke the hook of all the registered libraries.
        pipeline.libraries.forEach { library -> library.hooks?.let(::invokeHook) }
    }
}
