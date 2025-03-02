package eu.iamgio.quarkdown.cli.creator.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import eu.iamgio.quarkdown.cli.creator.ProjectCreator
import eu.iamgio.quarkdown.cli.creator.content.DefaultProjectCreatorInitialContentSupplier
import eu.iamgio.quarkdown.cli.creator.content.EmptyProjectCreatorInitialContentSupplier
import eu.iamgio.quarkdown.cli.creator.template.DefaultProjectCreatorTemplateProcessorFactory
import eu.iamgio.quarkdown.cli.util.saveTo
import eu.iamgio.quarkdown.document.DocumentAuthor
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.function.value.quarkdownName
import eu.iamgio.quarkdown.localization.Locale
import eu.iamgio.quarkdown.localization.LocaleLoader
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

    private val authorsRaw: String by option("--authors", help = "Project authors")
        .prompt("Authors (separated by commas)")

    private val authors: List<DocumentAuthor> by lazy {
        authorsRaw
            .split(",")
            .filter { it.isNotBlank() }
            .map { DocumentAuthor(it.trim()) }
    }

    private val type: DocumentType by option("--type", help = "Document type")
        .enum<DocumentType> { it.quarkdownName }
        .prompt(
            "Document type (${DocumentType.entries.joinToString("/") { it.quarkdownName }})",
            default = DocumentType.PAGED,
            showDefault = false,
        )

    private fun findLocale(language: String): Locale? = LocaleLoader.SYSTEM.find(language)

    private val languageRaw: String? by option("--lang", help = "Document language")
        .prompt("Document language")
        .check(
            lazyMessage = { "$it is not a valid locale." },
            validator = { it.isBlank() || findLocale(it) != null },
        )

    private val language: Locale? by lazy {
        languageRaw?.let(::findLocale)
    }

    private val noInitialContent: Boolean by option("-e", "--empty", help = "Do not include initial content")
        .flag()

    override fun run() {
        val info =
            DocumentInfo(
                name = name?.takeUnless { it.isBlank() } ?: directory.name,
                authors = authors.toMutableList(),
                type = type,
                locale = language,
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
