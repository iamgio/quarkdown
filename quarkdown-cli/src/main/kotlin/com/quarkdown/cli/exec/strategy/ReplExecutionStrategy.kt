package com.quarkdown.cli.exec.strategy

import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.output.OutputResource

/**
 * A strategy to execute a [Pipeline] in a continuous REPL (Read-Eval-Print Loop) mode.
 * The pipeline's context is shared across iterations, so definitions persist from one input to the next.
 * @param readLine supplier of the next input line, or `null` when the input is exhausted. Defaults to standard input
 */
class ReplExecutionStrategy(
    private val readLine: () -> String? = ::readlnOrNull,
) : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline): OutputResource? {
        println("== Quarkdown REPL ==")
        println("Type 'exit' to quit.")

        while (true) {
            print("\n> ")

            when (val input = readLine()) {
                null, "exit" -> break
                else -> pipeline.execute(input)
            }
        }

        // No output resources are generated in REPL mode.
        return null
    }
}
