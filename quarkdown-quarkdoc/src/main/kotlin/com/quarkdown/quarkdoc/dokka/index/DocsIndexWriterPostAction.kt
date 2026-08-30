package com.quarkdown.quarkdoc.dokka.index

import com.quarkdown.quarkdoc.reader.json.DOCS_INDEX_FILE_NAME
import com.quarkdown.quarkdoc.reader.json.DocsIndex
import kotlinx.serialization.json.Json
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.PostAction

/**
 * Post-rendering action that writes the documentation index, [DOCS_INDEX_FILE_NAME],
 * into the output directory root.
 *
 * Function data comes from the Dokka model via [DocsIndexCollectorTransformer];
 * each function's rendered page provides its content, converted to Markdown.
 */
class DocsIndexWriterPostAction(
    private val context: DokkaContext,
) : PostAction {
    override fun invoke() {
        val outputDirectory = context.configuration.outputDir

        val functions =
            DokkaHtmlWalker(outputDirectory)
                .walk()
                .filter { it.isInModule }
                .mapNotNull { result ->
                    DocsIndexStorage.find(result.moduleName, result.name)?.copy(
                        contentMarkdown =
                            result
                                .extractor()
                                .extractContent()
                                ?.let(HtmlToMarkdown::convert),
                    )
                }.toList()

        if (functions.isEmpty()) return

        outputDirectory
            .resolve(DOCS_INDEX_FILE_NAME)
            .writeText(Json.encodeToString(DocsIndex(functions)))
    }
}
