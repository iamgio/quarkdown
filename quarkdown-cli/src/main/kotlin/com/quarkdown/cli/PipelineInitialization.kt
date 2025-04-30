package com.quarkdown.cli

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.log.DebugFormatter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.stdlib.Stdlib

/**
 * Utility to initialize a [Pipeline].
 */
object PipelineInitialization {
    /**
     * Initializes a [Pipeline] with the given [flavor].
     * @param flavor flavor to use across the pipeline
     * @param loadableLibraryExporters exporters of external libraries that can be loaded by the user
     * @return the new pipeline
     */
    fun init(
        flavor: MarkdownFlavor,
        loadableLibraryExporters: Set<LibraryExporter>,
        options: PipelineOptions,
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
                    Log.info(output)
                },
            )

        // The pipeline.
        return Pipeline(
            context = MutableContext(flavor, loadableLibraries = loadableLibraries),
            options = options,
            libraries = libraries,
            renderer = { rendererFactory, context -> rendererFactory.html(context) },
            hooks = hooks,
        )
    }
}
