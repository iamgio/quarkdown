package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.pipeline.Pipeline
import java.io.File

/**
 * A strategy to execute a [Pipeline] from the string content of a file.
 */
class FileExecutionStrategy(private val file: File) : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline) {
        pipeline.execute(file.readText())
    }
}
