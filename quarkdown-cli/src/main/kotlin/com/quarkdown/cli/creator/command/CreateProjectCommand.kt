package com.quarkdown.cli.creator.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.quarkdown.cli.creator.ProjectCreator
import com.quarkdown.cli.creator.content.DefaultProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.DefaultTheme
import com.quarkdown.cli.creator.content.DocsProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.content.EmptyProjectCreatorInitialContentSupplier
import com.quarkdown.cli.creator.template.DefaultProjectCreatorTemplateProcessorFactory
import com.quarkdown.cli.creator.template.DocsProjectCreatorTemplateProcessorFactory
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.quarkdownName
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.output.visitor.saveTo
import java.io.File

/**
 * Default name of the default directory to save the generated files in.
 */
private const val DEFAULT_DIRECTORY = "."

/**
 * Default name of the main file, if not specified by the user.
 */
private const val DEFAULT_MAIN_FILE_NAME = "main"

/**
 * Command to create a new Quarkdown project with a default template.
 */
class CreateProjectCommand : CliktCommand("create") {
    private val directory: File by argument(help = "Project directory")
        .file(
            canBeFile = false,
            canBeDir = true,
            mustExist = false,
        ).default(File(DEFAULT_DIRECTORY))

    private val mainFileName: String? by option("--main-file", help = "Main file name")

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
            default = DocumentType.PLAIN,
        )

    private val description: String by option("--description", help = "Document description")
        .prompt("Document description (optional)")

    private val keywordsRaw: String? by option("--keywords", help = "Document keywords (comma-separated)")

    private val keywords: List<String> by lazy {
        keywordsRaw
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { it.trim() }
            ?: emptyList()
    }

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

    private val colorTheme: String? by option("--color-theme", help = "Color theme")

    private val layoutTheme: String? by option("--layout-theme", help = "Layout theme")

    private val noInitialContent: Boolean by option("-e", "--empty", help = "Do not include initial content")
        .flag()

    private fun createDocumentInfo() =
        DocumentInfo(
            name = name?.takeUnless { it.isBlank() } ?: directory.name,
            description = description.takeUnless { it.isBlank() },
            authors = authors.toMutableList(),
            keywords = keywords,
            type = type,
            locale = language,
            theme =
                DocumentTheme(
                    colorTheme ?: DefaultTheme.getColorTheme(type),
                    layoutTheme ?: DefaultTheme.getLayoutTheme(type),
                ),
        )

    private fun createProjectCreator(): ProjectCreator {
        val mainFileName = this.mainFileName ?: DEFAULT_MAIN_FILE_NAME
        val documentInfo = this.createDocumentInfo()
        val isDocs = documentInfo.type == DocumentType.DOCS
        return ProjectCreator(
            templateProcessorFactory =
                when {
                    isDocs -> DocsProjectCreatorTemplateProcessorFactory(documentInfo)
                    else -> DefaultProjectCreatorTemplateProcessorFactory(documentInfo)
                },
            initialContentSupplier =
                when {
                    noInitialContent -> EmptyProjectCreatorInitialContentSupplier()
                    isDocs -> DocsProjectCreatorInitialContentSupplier()
                    else -> DefaultProjectCreatorInitialContentSupplier()
                },
            mainFileName,
        )
    }

    override fun run() {
        val creator = this.createProjectCreator()

        directory.mkdirs()
        creator.createResources().forEach { it.saveTo(directory) }

        Log.info("Project created into ${directory.canonicalPath}")
    }
}
