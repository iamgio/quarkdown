package com.quarkdown.core.pipeline.stage

/**
 * A specialized pipeline stage that "peeks" at the input without modifying it.
 *
 * This interface is useful for stages that need to perform some operation on the input
 * (such as validation, logging, or side effects) but don't need to transform it into a different object.
 *
 * The [process] method is implemented to call [peek] and then return the input unchanged.
 *
 * @param T the type of both the input and output
 */
interface PeekPipelineStage<T> : PipelineStage<T, T> {
    /**
     * Processes the input by calling [peek] and then returning the input unchanged.
     *
     * @param input the input to process
     * @param data shared data that is passed between pipeline stages
     * @return the input, unchanged
     */
    override fun process(
        input: T,
        data: SharedPipelineData,
    ): T {
        peek(input, data)
        return input
    }

    /**
     * Peeks at the input without modifying it.
     *
     * This method is called by [process] and should perform any operations needed
     * on the input without changing it.
     *
     * @param input the input to peek at
     * @param data shared data that is passed between pipeline stages
     */
    fun peek(
        input: T,
        data: SharedPipelineData,
    )
}

/**
 * Utility function to execute a pipeline stage that takes `Unit` as input.
 *
 * This is a convenience function for pipeline stages that don't require any input
 * other than the shared pipeline data.
 *
 * @param data shared data that is passed between pipeline stages
 * @return the output of the pipeline stage
 */
fun <O> PipelineStage<Unit, O>.execute(data: SharedPipelineData): O = execute(Unit, data)
