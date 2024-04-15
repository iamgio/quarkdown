package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline

/**
 * A strategy to execute a [Pipeline] in a continuous REPL (Read-Eval-Print Loop) mode.
 * Note that the context is shared across iterations.
 */
class ReplExecutionStrategy : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline) {
        Log.info("== Quarkdown REPL ==")
        Log.info("Type 'exit' to quit.")
        Log.info("Tip: pass the source file path as an argument to execute it instead.")

        while (true) {
            print("\n> ")

            when (val input = readlnOrNull()) {
                null, "exit" -> break
                else -> pipeline.execute(input)
            }
        }
    }
}
