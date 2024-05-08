package eu.iamgio.quarkdown.pipeline.output

import java.io.File

/**
 * A visitor that saves each type of [OutputResource] to a file and returns it.
 * @param location directory to save the resources to
 */
class FileResourceExporter(private val location: File) : OutputResourceVisitor<File> {
    /**
     * Saves an [OutputArtifact] to a file with text content.
     * @return the file itself
     */
    override fun visit(artifact: OutputArtifact) =
        File(location, artifact.name + "." + artifact.type.name.lowercase()).also {
            it.writeText(artifact.content.toString())
        }

    /**
     * Saves an [OutputResourceGroup] to a directory which contains its nested files.
     * @return the directory file itself
     */
    override fun visit(group: OutputResourceGroup): File {
        val directory = File(location, group.name)
        directory.mkdirs()

        // Saves the subfiles in the new directory.
        group.resources.forEach {
            it.accept(FileResourceExporter(directory))
        }

        return directory
    }
}
