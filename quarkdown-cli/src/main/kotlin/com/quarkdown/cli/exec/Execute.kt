package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import com.quarkdown.cli.util.cleanDirectory
import com.quarkdown.cli.util.runWithTimeout
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.visitor.saveTo
import com.quarkdown.core.pipeline.session.QuarkdownSession

/**
 * Executes a complete Quarkdown pipeline in [session], over a fresh context that is closed once done.
 *
 * This is the entry point used by both the CLI and any in-process embedder.
 * On any [PipelineException] the exception propagates to the caller, for embedders to catch and handle.
 * If [CliOptions.timeoutSeconds] is set and expires, an [ExecutionTimeoutException] is raised instead.
 *
 * @param executionStrategy launch strategy of the pipeline, e.g. from file or REPL
 * @param session session to run the pipeline in
 * @param cliOptions options that define the behavior of the CLI, especially I/O and the execution timeout
 * @return the outcome of the executed pipeline, carrying the produced resource, directory (if any) and context
 * @throws PipelineException if the pipeline fails and its error handler rethrows the error
 * @throws ExecutionTimeoutException if [CliOptions.timeoutSeconds] is set and the execution exceeds it
 */
fun runQuarkdown(
    executionStrategy: PipelineExecutionStrategy,
    session: QuarkdownSession,
    cliOptions: CliOptions,
): ExecutionOutcome {
    // Output directory to save the generated resources in.
    val outputDirectory = cliOptions.outputDirectory

    // Cleans the output directory if enabled in options.
    if (cliOptions.clean) {
        outputDirectory?.cleanDirectory()
    }

    // Pipeline execution.
    return runWithTimeout(cliOptions.timeoutSeconds) {
        session.createContext().use { context ->
            val resource = executionStrategy.execute(session.createPipeline(context))
            val childDirectory = outputDirectory?.let { resource?.saveTo(it) }
            ExecutionOutcome(resource, childDirectory, context)
        }
    }
}

/**
 * Executes a complete Quarkdown pipeline in a new session created from the given options.
 * @param executionStrategy launch strategy of the pipeline, e.g. from file or REPL
 * @param cliOptions options that define the behavior of the CLI
 * @param pipelineOptions options that define the behavior of the pipeline
 * @return the outcome of the executed pipeline
 * @see runQuarkdown
 * @see createSession
 */
fun runQuarkdown(
    executionStrategy: PipelineExecutionStrategy,
    cliOptions: CliOptions,
    pipelineOptions: PipelineOptions,
): ExecutionOutcome = runQuarkdown(executionStrategy, createSession(cliOptions, pipelineOptions), cliOptions)
