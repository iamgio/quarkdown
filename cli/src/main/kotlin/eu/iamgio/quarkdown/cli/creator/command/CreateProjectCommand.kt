package eu.iamgio.quarkdown.cli.creator.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import eu.iamgio.quarkdown.cli.creator.ProjectCreator
import eu.iamgio.quarkdown.cli.creator.content.DefaultProjectCreatorInitialContentSupplier
import eu.iamgio.quarkdown.cli.creator.content.EmptyProjectCreatorInitialContentSupplier
import eu.iamgio.quarkdown.cli.creator.template.DefaultProjectCreatorTemplateProcessorFactory
import eu.iamgio.quarkdown.cli.util.saveTo
import eu.iamgio.quarkdown.document.DocumentInfo
import java.io.File

/**
 * Default ame of the default directory to save the generated files in.
 */
private const val DEFAULT_DIRECTORY = "."

/**
 * Command to create a new Quarkdown project with a default template.
 */
class CreateProjectCommand : CliktCommand("create") {
    /**
     * Optional output directory.
     * If not set, the output is saved in [DEFAULT_DIRECTORY].
     */
    private val directory: File by argument(help = "Project directory")
        .file(
            canBeFile = false,
            canBeDir = true,
        ).default(File(DEFAULT_DIRECTORY))

    private val name: String? by option("--name", help = "Project name")
        .prompt("Project name")

    private val noInitialContent: Boolean by option("-e", "--empty", help = "Do not include initial content")
        .flag()

    override fun run() {
        val info =
            DocumentInfo(
                name = name,
            )

        val templateFactory = DefaultProjectCreatorTemplateProcessorFactory(info)
        val initialContentSupplier =
            when {
                noInitialContent -> EmptyProjectCreatorInitialContentSupplier()
                else -> DefaultProjectCreatorInitialContentSupplier()
            }

        val creator = ProjectCreator(templateFactory, initialContentSupplier, "main")

        directory.mkdirs()
        creator.createResources().forEach { it.saveTo(directory) }
    }
}
