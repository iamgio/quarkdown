package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.stdlib.Stdlib

/**
 * Utility to initialize a [Pipeline].
 */
object PipelineInitialization {
    /**
     * Initializes a [Pipeline] with the given [flavor].
     * @param flavor flavor to use across the pipeline
     * @return the new pipeline
     */
    fun init(flavor: MarkdownFlavor): Pipeline {
        // Libraries to load.
        val libraries = setOf(Stdlib.library)

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
                afterPostRendering = { output ->
                    Log.info(output)
                },
            )

        // The pipeline.
        return Pipeline(
            context = MutableContext(flavor),
            libraries = libraries,
            renderer = { rendererFactory, context -> rendererFactory.html(context) },
            hooks = hooks,
        )
    }
}
