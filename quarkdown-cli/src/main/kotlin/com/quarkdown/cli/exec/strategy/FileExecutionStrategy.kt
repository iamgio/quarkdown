package com.quarkdown.cli.exec.strategy

import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.output.OutputResource
import java.io.File

/**
 * A strategy to execute a [Pipeline] from the string content of a file.
 */
class FileExecutionStrategy(
    private val file: File,
) : PipelineExecutionStrategy {
    override fun execute(pipeline: Pipeline): OutputResource = pipeline.execute(file.readText())
}
