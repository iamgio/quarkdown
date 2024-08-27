package eu.iamgio.quarkdown.pipeline.output

import eu.iamgio.quarkdown.util.sanitize
import java.io.File

/**
 * A visitor that saves each type of [OutputResource] to a file and returns it.
 * @param location directory to save the resources to
 */
class FileResourceExporter(private val location: File) : OutputResourceVisitor<File> {
    /**
     * Name of the corresponding file of this resource, without the extension,
     * with symbols removed and spaces replaced with dashes.
     */
    private val OutputResource.fileName: String
        get() =
            name.replace("\\s+".toRegex(), "-")
                .sanitize(replacement = "")

    /**
     * File extension relative to the [ArtifactType] of this resource.
     */
    private val TypedOutputResource.fileExtension: String
        get() =
            when (type) {
                ArtifactType.HTML -> "html"
                ArtifactType.CSS -> "css"
                ArtifactType.JAVASCRIPT -> "js"
                ArtifactType.AUTO -> "" // Assumes the file name already contains an extension.
            }

    /**
     * Full name of the file, including the extension, relative to the [ArtifactType] of this resource.
     */
    private val TypedOutputResource.fullFileName: String
        get() = "$fileName.$fileExtension"

    /**
     * Saves an [OutputArtifact] to a file with text content.
     * @return the file itself
     */
    override fun visit(artifact: OutputArtifact) =
        File(location, artifact.fullFileName).also {
            it.writeText(artifact.content.toString())
        }

    override fun visit(artifact: BinaryOutputArtifact) =
        File(location, artifact.fullFileName).also {
            it.writeBytes(artifact.content)
        }

    /**
     * Saves an [OutputResourceGroup] to a directory which contains its nested files.
     * @return the directory file itself
     */
    override fun visit(group: OutputResourceGroup): File {
        val directory = File(location, group.fileName)
        directory.mkdirs()

        // Saves the subfiles in the new directory.
        group.resources.forEach {
            it.accept(FileResourceExporter(directory))
        }

        return directory
    }
}
