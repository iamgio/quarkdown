package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.NO_SOURCE_FILE_EXIT_CODE
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import eu.iamgio.quarkdown.stdlib.Stdlib
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

    // Pipeline initialization.
    val pipeline =
        Pipeline(
            context = MutableContext(flavor),
            libraries = libraries,
            renderer = { rendererFactory, context -> rendererFactory.html(context) },
            hooks = hooks,
        )

    try {
        val source = sourceFile.readText()
        pipeline.execute(source)
    } catch (e: PipelineException) {
        e.printStackTrace()
        exitProcess(e.code)
    }
}
