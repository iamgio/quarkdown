package eu.iamgio.quarkdown.cli.exec.strategy

import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import java.io.File

/**
 * A strategy to execute a [Pipeline] from the string content of a file.
 */
class FileExecutionStrategy(private val file: File) : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline): OutputResource {
        return pipeline.execute(file.readText())
    }
}
