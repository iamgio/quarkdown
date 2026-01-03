package com.quarkdown.rendering.html

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.graph.DirectedGraph
import com.quarkdown.core.graph.Graph
import com.quarkdown.core.graph.VisitableOnceGraph
import com.quarkdown.core.pipeline.Pipelines
import com.quarkdown.rendering.html.search.SearchEntry
import com.quarkdown.rendering.html.search.SearchHeading
import com.quarkdown.rendering.html.search.SearchIndex
import com.quarkdown.rendering.html.search.SearchIndexGenerator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [SearchIndexGenerator].
 */
class SearchIndexGeneratorTest {
    @BeforeTest
    fun setup() {
        Pipelines.clear()
    }

    @Test
    fun `single subdocument`() {
        val context = MutableContext(QuarkdownFlavor, subdocument = Subdocument.Root)
        val graph: Graph<Subdocument> = DirectedGraph<Subdocument>().addVertex(Subdocument.Root)
        context.documentInfo =
            DocumentInfo(name = "Test Document", description = "A test document", keywords = listOf("test", "document"))
        context.subdocumentGraph = VisitableOnceGraph(graph)
        context.attachMockPipeline()

        val index = SearchIndexGenerator.generate(graph)

        assertEquals(
            SearchIndex(
                entries =
                    listOf(
                        SearchEntry(
                            url = "/",
                            title = "Test Document",
                            description = "A test document",
                            keywords = listOf("test", "document"),
                            headings = emptyList(),
                        ),
                    ),
            ),
            index,
        )
    }

    @Test
    fun `two subdocuments`() {
        val subdoc1 = Subdocument.Root
        val subdoc2 = Subdocument.Resource(name = "child", path = "", content = "")

        val context1 = MutableContext(QuarkdownFlavor, subdocument = subdoc1)
        val context2 = MutableContext(QuarkdownFlavor, subdocument = subdoc2)

        val graph: Graph<Subdocument> =
            DirectedGraph<Subdocument>()
                .addVertex(subdoc1)
                .addVertex(subdoc2)
                .addEdge(subdoc1, subdoc2)

        context1.documentInfo =
            DocumentInfo(
                name = "Root Document",
                description = "The root document",
                keywords = listOf("root", "document"),
            )

        context2.documentInfo =
            DocumentInfo(
                name = "Child Document",
                description = "A child document",
                keywords = listOf("child", "document"),
            )

        context1.subdocumentGraph = VisitableOnceGraph(graph)
        context2.subdocumentGraph = VisitableOnceGraph(graph)
        context1.attachMockPipeline()
        context2.attachMockPipeline()

        val index = SearchIndexGenerator.generate(graph)

        assertEquals(
            SearchIndex(
                entries =
                    listOf(
                        SearchEntry(
                            url = "/",
                            title = "Root Document",
                            description = "The root document",
                            keywords = listOf("root", "document"),
                            headings = emptyList(),
                        ),
                        SearchEntry(
                            url = "/child",
                            title = "Child Document",
                            description = "A child document",
                            keywords = listOf("child", "document"),
                            headings = emptyList(),
                        ),
                    ),
            ),
            index,
        )
    }

    @Test
    fun `subdocument with headings`() {
        val context = MutableContext(QuarkdownFlavor, subdocument = Subdocument.Root)
        val graph: Graph<Subdocument> = DirectedGraph<Subdocument>().addVertex(Subdocument.Root)
        context.documentInfo =
            DocumentInfo(
                name = "Document with Headings",
                description = "A document that has headings",
                keywords = listOf("headings"),
            )
        context.attributes.tableOfContents =
            TableOfContents.generate(
                sequenceOf(
                    Heading(depth = 1, text = buildInline { text("Heading 1") }),
                    Heading(depth = 2, text = buildInline { text("Heading 2") }),
                ),
            )

        context.subdocumentGraph = VisitableOnceGraph(graph)
        context.attachMockPipeline()

        val index = SearchIndexGenerator.generate(graph)

        assertEquals(
            SearchIndex(
                entries =
                    listOf(
                        SearchEntry(
                            url = "/",
                            title = "Document with Headings",
                            description = "A document that has headings",
                            keywords = listOf("headings"),
                            headings =
                                listOf(
                                    SearchHeading(
                                        anchor = "heading-1",
                                        text = "Heading 1",
                                        level = 1,
                                    ),
                                    SearchHeading(
                                        anchor = "heading-2",
                                        text = "Heading 2",
                                        level = 2,
                                    ),
                                ),
                        ),
                    ),
            ),
            index,
        )
    }
}
