package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryExporter
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.stdlib.Stdlib

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
