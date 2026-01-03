package com.quarkdown.rendering.html.search

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.graph.Graph
import com.quarkdown.core.pipeline.Pipelines
import com.quarkdown.core.util.toPlainText
import com.quarkdown.rendering.html.HtmlIdentifierProvider

/**
 * Generates a [SearchIndex] from the subdocument graph of a multi-document project.
 * The generated index is intended to be serialized to JSON and used for client-side search.
 */
object SearchIndexGenerator {
    /**
     * Generates a search index from the given subdocument graph.
     * Each subdocument becomes a [SearchEntry] containing its URL, metadata, and headings.
     * @param graph the subdocument graph representing the documentation structure
     * @param contextLookup a function to retrieve the [Context] for a given [Subdocument]
     * @return a [SearchIndex] containing all searchable entries
     */
    fun generate(
        graph: Graph<Subdocument>,
        contextLookup: (Subdocument) -> Context? = { subdocument -> Pipelines.allContexts.find { it.subdocument == subdocument } },
    ): SearchIndex {
        val subdocuments = graph.vertices

        return SearchIndex(
            entries =
                subdocuments.mapNotNull { subdocument ->
                    val context = contextLookup(subdocument) ?: return@mapNotNull null

                    SearchEntry(
                        url = "/" + if (subdocument is Subdocument.Root) "" else subdocument.getOutputFileName(context),
                        title = context.documentInfo.name,
                        description = context.documentInfo.description,
                        keywords = context.documentInfo.keywords,
                        headings = getHeadings(context),
                    )
                },
        )
    }

    private fun flatten(item: TableOfContents.Item): Sequence<TableOfContents.Item> =
        sequenceOf(item) +
            item.subItems.asSequence().flatMap(::flatten)

    private fun getHeadings(context: Context): List<SearchHeading> {
        val toc = context.attributes.tableOfContents ?: return emptyList()

        return toc.items
            .asSequence()
            .flatMap(::flatten)
            .filterNot { it.isDecorative }
            .map { item ->
                SearchHeading(
                    anchor = item.target.accept(HtmlIdentifierProvider.of(renderer = null)),
                    text = item.text.toPlainText(),
                    level = item.depth,
                )
            }.toList()
    }
}
