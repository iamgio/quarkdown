package com.quarkdown.core.pipeline.session

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.output.OutputResource

/**
 * A long-lived compilation environment, where a template [Pipeline] is set up once,
 * and every compilation runs a copy of it over its own context.
 * @param template the pipeline every compilation is a copy of
 */
class QuarkdownSession(
    private val template: Pipeline,
) {
    /**
     * Creates a fresh context with the template's flavor and loadable libraries.
     * The caller must [MutableContext.close] it once no pipeline has to run on it anymore.
     * @return the new context
     */
    fun createContext(): MutableContext =
        with(template.readOnlyContext) {
            MutableContext(flavor, loadableLibraries = loadableLibraries)
        }

    /**
     * Creates a copy of the template pipeline bound to [context].
     * The same pipeline can be executed repeatedly on its context, with definitions persisting between executions.
     * @param context the context the pipeline runs on
     * @return the new pipeline
     */
    fun createPipeline(context: MutableContext): Pipeline = template.copy(context)

    /**
     * Compiles [source] in a fresh context, released afterward.
     * @param source Quarkdown source code
     * @return the generated output resource, if any
     */
    fun compile(source: CharSequence): OutputResource? = createContext().use { createPipeline(it).execute(source) }
}
