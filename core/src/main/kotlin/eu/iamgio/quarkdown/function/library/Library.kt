package eu.iamgio.quarkdown.function.library

import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.pipeline.PipelineHooks

/**
 * A bundle of functions that can be called from a Quarkdown source.
 * @param name name of the library
 * @param functions functions the library makes available to call
 * @param hooks optional actions to run after each stage of a pipeline where this library is registered in has been completed
 */
data class Library(
    val name: String,
    val functions: Set<Function<*>>,
    val hooks: PipelineHooks? = null,
) {
    /**
     * @return a copy of this library with the given pipeline hooks attached
     */
    fun withHooks(hooks: PipelineHooks) = copy(hooks = hooks)
}
