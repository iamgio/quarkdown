package eu.iamgio.quarkdown.cli.exec.strategy

import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.output.OutputResource

/**
 * A strategy to execute a [Pipeline] in a continuous REPL (Read-Eval-Print Loop) mode.
 * Note that the context is shared across iterations.
 */
class ReplExecutionStrategy : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline): OutputResource? {
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

        // No output resources are generated in REPL mode.
        return null
    }
}
