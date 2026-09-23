package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.lib.QdLibraries
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.log.DebugFormatter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.session.QuarkdownSession
import com.quarkdown.stdlib.Stdlib

/**
 * Creates the session every CLI compilation runs in.
 * A session is created once per command and reused across live preview recompilations.
 * @param cliOptions options that define the behavior of the CLI
 * @param pipelineOptions options that define the behavior of the pipeline
 * @return the new session
 */
fun createSession(
    cliOptions: CliOptions,
    pipelineOptions: PipelineOptions,
): QuarkdownSession {
    val loadableLibraries: Set<LibraryExporter> =
        try {
            cliOptions.libraryDirectory?.let(QdLibraries::fromDirectory) ?: emptySet()
        } catch (e: Exception) {
            Log.warn(e.message ?: "")
            emptySet()
        }

    val hooks =
        PipelineHooks(
            afterRegisteringLibraries = { libs ->
                Log.debug { "Libraries: " + DebugFormatter.formatLibraries(libs) }
            },
            afterParsing = { document ->
                Log.debug { "AST:\n" + DebugFormatter.formatAST(document) }
            },
            afterAllRendering = { output ->
                Log.debug { "Final Output:\n$output" }
                if (cliOptions.pipe) {
                    println(output)
                }
            },
        )

    return QuarkdownSession(
        Pipeline(
            context = MutableContext(loadableLibraries = LibraryExporter.exportAll(*loadableLibraries.toTypedArray())),
            options = pipelineOptions,
            libraries = LibraryExporter.exportAll(Stdlib),
            renderer = cliOptions.renderer,
            hooks = hooks,
        ),
    )
}
