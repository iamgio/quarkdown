package eu.iamgio.quarkdown.cli.resource

import eu.iamgio.quarkdown.pipeline.output.BinaryOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.FileResourceExporter
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.pipeline.output.OutputResourceVisitor
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import java.io.File

/**
 * An [OutputResourceVisitor] that figures the root directory of the resource.
 * - Artifacts are resolved to their parent directory.
 * - Resource groups are resolved to their own directory.
 *
 * This is useful, for instance, to determine the directory that contains the `index.html` file
 * of an HTML output returned by a pipeline.
 *
 * @param location parent directory of the resources
 */
class OuputResourceDirectoryResolver(
    location: File,
) : OutputResourceVisitor<File> {
    private val fileExporter = FileResourceExporter(location, write = false)

    override fun visit(artifact: TextOutputArtifact): File = artifact.accept(fileExporter).parentFile

    override fun visit(artifact: BinaryOutputArtifact): File = artifact.accept(fileExporter).parentFile

    override fun visit(group: OutputResourceGroup): File = group.accept(fileExporter)
}
