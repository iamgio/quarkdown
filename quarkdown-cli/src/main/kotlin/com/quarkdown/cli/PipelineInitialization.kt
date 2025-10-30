package com.quarkdown.cli

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.log.DebugFormatter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.stdlib.Stdlib

/**
 * Utility to initialize a [Pipeline].
 */
object PipelineInitialization {
    /**
     * Initializes a [Pipeline] with the given [flavor].
     * @param flavor flavor to use across the pipeline
     * @param loadableLibraryExporters exporters of external libraries that can be loaded by the user
     * @param options options that define the behavior of the pipeline
     * @param printOutput whether to output the rendered result to standard output, suitable for piping
     * @param renderer function that provides the rendering components given a renderer factory and context
     * @return the new pipeline
     */
    fun init(
        flavor: MarkdownFlavor,
        loadableLibraryExporters: Set<LibraryExporter>,
        options: PipelineOptions,
        printOutput: Boolean,
        renderer: (RendererFactory, Context) -> RenderingComponents,
    ): Pipeline {
        // Libraries to load.
        val libraries: Set<Library> = LibraryExporter.exportAll(Stdlib)
        val loadableLibraries: Set<Library> = LibraryExporter.exportAll(*loadableLibraryExporters.toTypedArray())

        // Actions run after each stage of the pipeline.
        val hooks =
            PipelineHooks(
                afterRegisteringLibraries = { libs ->
                    Log.debug { "Libraries: " + DebugFormatter.formatLibraries(libs) }
                },
                afterLexing = { tokens ->
                    Log.debug { "Tokens:\n" + DebugFormatter.formatTokens(tokens) }
                },
                afterParsing = { document ->
                    Log.debug { "AST:\n" + DebugFormatter.formatAST(document) }
                },
                afterRendering = { output ->
                    if (printOutput) {
                        println(output)
                    }
                },
            )

        // The pipeline.
        return Pipeline(
            context = MutableContext(flavor, loadableLibraries = loadableLibraries),
            options = options,
            libraries = libraries,
            renderer = renderer,
            hooks = hooks,
        )
    }
}
