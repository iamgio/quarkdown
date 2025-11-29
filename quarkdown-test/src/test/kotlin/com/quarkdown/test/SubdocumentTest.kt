package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val NON_EXISTENT_FUNCTION = "somenonexistentfunction"

/**
 * Tests for subdocument generation.
 */
class SubdocumentTest {
    private fun subdoc(
        name: String,
        content: String,
    ) = Subdocument.Resource(
        name = name,
        path = name,
        content = content,
    )

    private val simpleSubdoc = subdoc("subdoc1", content = "Content")
    private val referenceToParentSubdoc = subdoc("subdoc2", content = ".$NON_EXISTENT_FUNCTION")
    private val definitionSubdoc = subdoc("subdoc3", content = ".function {$NON_EXISTENT_FUNCTION}\n\thello")
    private val thirdPartySubdoc = subdoc("subdoc3", content = ".mermaid\n\tgraph TD\n\t\tA-->B")

    private fun getResource(
        group: OutputResource?,
        subdocument: Subdocument,
        context: Context,
    ): TextOutputArtifact {
        val resources = getSubResources(group)
        val resource =
            resources.firstOrNull { it.name == subdocument.getOutputFileName(context) }
                as? TextOutputArtifact
        assertNotNull(resource)
        return resource
    }

    private fun getTextResourceCount(group: OutputResource?): Int = getSubResources(group).filterIsInstance<TextOutputArtifact>().size

    @Test
    fun `root to subdocument`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(simpleSubdoc).addEdge(Subdocument.Root, simpleSubdoc) },
            outputResourceHook = { group ->
                val resource = getResource(group, simpleSubdoc, this)
                assertContains(resource.content, "<html>")
                assertEquals(2, subdocumentGraph.vertices.size)
                assertEquals(2, getTextResourceCount(group))
                assertContains(getSubResources(group).map { it.name }, simpleSubdoc.name)
            },
        ) {}
    }

    @Test
    fun `collision-proof subdocument name`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(simpleSubdoc).addEdge(Subdocument.Root, simpleSubdoc) },
            minimizeSubdocumentCollisions = true,
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, simpleSubdoc.uniqueName)
                assertFalse(simpleSubdoc.name in resources)
            },
        ) {}
    }

    @Test
    fun `context should be shared to subdocument`() {
        execute(
            """
            .doctype {paged}
            
            .function {$NON_EXISTENT_FUNCTION}
              hello
            """.trimIndent(),
            subdocumentGraph = {
                it.addVertex(referenceToParentSubdoc).addEdge(Subdocument.Root, referenceToParentSubdoc)
            },
            outputResourceHook = { group ->
                val resource = getResource(group, referenceToParentSubdoc, this)
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertContains(resource.content, "paged")
            },
        ) {}
    }

    @Test
    fun `context should not be shared from subdocument to parent`() {
        execute(
            ".doctype {paged}",
            subdocumentGraph = { it.addVertex(definitionSubdoc).addEdge(Subdocument.Root, definitionSubdoc) },
            outputResourceHook = {
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertNull(getFunctionByName(NON_EXISTENT_FUNCTION))
            },
        ) {}
    }

    @Test
    fun `third-party presence should be shared from subdocument to parent`() {
        execute(
            "",
            subdocumentGraph = { it.addVertex(thirdPartySubdoc).addEdge(Subdocument.Root, thirdPartySubdoc) },
            outputResourceHook = {
                assertTrue(attributes.hasMermaidDiagram)
            },
        ) {}
    }

    @Test
    fun `simple subdocument from file`() {
        arrayOf(
            "The link is: [1](subdoc/simple-1.qd)",
            "The link is: .subdocument {subdoc/simple-1.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getTextResourceCount(it))
                },
            ) {
                if (subdocument == Subdocument.Root) {
                    assertEquals("<p>The link is: <a href=\"./simple-1.html\">1</a></p>", it)
                }
            }
        }
    }

    @Test
    fun `empty label subdocument from file`() {
        arrayOf(
            "The link is: [](subdoc/simple-1.qd)",
            "The link is: .subdocument {subdoc/simple-1.qd}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getTextResourceCount(it))
                },
            ) {
                if (subdocument == Subdocument.Root) {
                    assertEquals("<p>The link is: <a href=\"./simple-1.html\"></a></p>", it)
                }
            }
        }
    }

    @Test
    fun `root to gateway to 1 and 2`() {
        arrayOf(
            "[Gateway](subdoc/gateway.qd)",
            ".subdocument {subdoc/gateway.qd} label:{Gateway}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(4, subdocumentGraph.vertices.size)
                    assertEquals(4, getTextResourceCount(it))
                },
            ) {}
        }
    }

    @Test
    fun `circular, root to 1 to 2 to 1`() {
        arrayOf(
            "[1](subdoc/circular-1.qd)",
            ".subdocument {subdoc/circular-1.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(3, subdocumentGraph.vertices.size)
                    assertEquals(3, getTextResourceCount(it))
                },
            ) {}
        }
    }

    @Test
    fun `recursive, root to 1 recursively`() {
        arrayOf(
            "[1](subdoc/recursive.qd)",
            ".subdocument {subdoc/recursive.qd} label:{1}",
        ).forEach { source ->
            execute(
                source,
                outputResourceHook = {
                    assertEquals(2, subdocumentGraph.vertices.size)
                    assertEquals(2, getTextResourceCount(it))
                },
            ) {}
        }
    }
}
