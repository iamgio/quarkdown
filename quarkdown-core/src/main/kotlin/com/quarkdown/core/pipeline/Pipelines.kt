package com.quarkdown.core.pipeline

import com.quarkdown.core.context.Context

/**
 * Storage for keeping track of active pipelines.
 */
object Pipelines {
    /**
     * 1-1 associations between contexts and their pipeline.
     */
    private val pipelines: MutableMap<Context, Pipeline> = mutableMapOf()

    /**
     * @param context context to retrieve the pipeline from
     * @return the pipeline attached to [context], if it exists.
     *         A context can only have up to one attached pipeline.
     */
    fun getAttachedPipeline(context: Context): Pipeline? = pipelines[context]

    /**
     * Attaches a pipeline to a context.
     * @param context context to attach the pipeline to
     * @param pipeline pipeline to attach
     * @throws IllegalStateException if [context] already has an attached pipeline
     */
    fun attach(
        context: Context,
        pipeline: Pipeline,
    ) {
        if (context in pipelines) {
            throw IllegalStateException("Context already has an attached pipeline.")
        }

        pipelines[context] = pipeline
    }
}
