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
     * Attaches a pipeline to a context. Attaching the same pipeline again is a no-op,
     * which lets a pipeline be executed repeatedly on its context.
     * @param context context to attach the pipeline to
     * @param pipeline pipeline to attach
     * @throws IllegalStateException if [context] already has a different attached pipeline
     */
    fun attach(
        context: Context,
        pipeline: Pipeline,
    ) {
        val attached = pipelines[context]
        if (attached != null && attached !== pipeline) {
            throw IllegalStateException("Context already has an attached pipeline.")
        }

        pipelines[context] = pipeline
    }

    /**
     * Detaches the pipeline of [context] and of every subdocument context that shares its data,
     * releasing them from this storage. A context can be attached again afterwards.
     * @param context root context whose pipelines are no longer active
     */
    fun detach(context: Context) {
        context.sharedSubdocumentsData.withContexts.values
            .forEach(pipelines::remove)
        pipelines.remove(context)
    }
}
