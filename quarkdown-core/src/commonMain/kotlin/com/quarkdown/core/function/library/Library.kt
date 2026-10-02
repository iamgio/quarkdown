package com.quarkdown.core.function.library

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.pipeline.PipelineHooks

/**
 * A bundle of functions that can be called from a Quarkdown source.
 * @param name name of the library
 * @param functions functions the library makes available to call
 * @param onLoad optional action to run when the library is loaded in a context. Returns an optional value to be used as the result of loading the library
 * @param hooks optional actions to run after each stage of a pipeline where this library is registered in has been completed
 */
data class Library(
    val name: String,
    val functions: Set<Function<*>>,
    val onLoad: ((Context) -> OutputValue<*>)? = null,
    val hooks: PipelineHooks? = null,
) {
    /**
     * [functions] indexed by name. On duplicate names, the first declared function is kept.
     */
    private val functionsByName: Map<String, Function<*>> by lazy {
        functions.distinctBy { it.name }.associateBy { it.name }
    }

    /**
     * @param name name of the function to find
     * @return the function of this library named [name], if any
     */
    fun findFunction(name: String): Function<*>? = functionsByName[name]

    /**
     * @return a copy of this library with the given pipeline hooks attached
     */
    fun withHooks(hooks: PipelineHooks) = copy(hooks = hooks)
}
