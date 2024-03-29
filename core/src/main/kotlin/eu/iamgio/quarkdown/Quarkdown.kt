package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Log.error("No source file passed.")
        exitProcess(NO_SOURCE_FILE_EXIT_CODE)
    }

    val sourceFile = File(args.first())

    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // Actions run after each stage of the pipeline.
    val hooks =
        PipelineHooks(
            afterLexing = { tokens ->
                Log.debug { "Tokens:\n" + DebugFormatter.formatTokens(tokens) }
            },
            afterParsing = { document ->
                Log.debug { "AST:\n" + DebugFormatter.formatAST(document) }
            },
            afterRendering = { rendered ->
                Log.info(rendered)
            },
        )

    // Pipeline initialization.
    val pipeline =
        Pipeline(
            source = sourceFile.readText(),
            flavor = flavor,
            renderer = { rendererFactory, context -> rendererFactory.html(context) },
            hooks = hooks,
        )

    pipeline.execute()
}
