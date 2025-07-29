package com.quarkdown.test

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    private fun getResource(
        group: OutputResource?,
        subdocument: Subdocument,
    ): TextOutputArtifact {
        val resources = getSubResources(group)
        val resource =
            resources.firstOrNull { it.name == subdocument.uniqueName }
                as? TextOutputArtifact
        assertNotNull(resource)
        return resource
    }

    @Test
    fun `root to subdocument`() {
        execute(
            source = "",
            subdocumentGraph = { it.addVertex(simpleSubdoc).addEdge(Subdocument.Root, simpleSubdoc) },
            outputResourceHook = { group ->
                val resource = getResource(group, simpleSubdoc)
                assertContains(resource.content, "<html>")
                assertEquals(2, subdocumentGraph.vertices.size)
            },
        ) {}
    }

    @Test
    fun `context should be shared to subdocument`() {
        execute(
            source =
                """
                .doctype {paged}
                
                .function {$NON_EXISTENT_FUNCTION}
                  hello
                """.trimIndent(),
            subdocumentGraph = {
                it.addVertex(referenceToParentSubdoc).addEdge(Subdocument.Root, referenceToParentSubdoc)
            },
            outputResourceHook = { group ->
                val resource = getResource(group, referenceToParentSubdoc)
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertContains(resource.content, "paged")
            },
        ) {}
    }

    @Test
    fun `context should not be shared from subdocument to parent`() {
        execute(
            source = ".doctype {paged}",
            subdocumentGraph = { it.addVertex(definitionSubdoc).addEdge(Subdocument.Root, definitionSubdoc) },
            outputResourceHook = {
                assertEquals(DocumentType.PAGED, documentInfo.type)
                assertNull(getFunctionByName(NON_EXISTENT_FUNCTION))
            },
        ) {}
    }

    @Test
    fun `simple subdocument from file`() {
        execute(
            source = "[1](subdoc/simple-1.qd)",
            outputResourceHook = {
                assertEquals(2, subdocumentGraph.vertices.size)
            },
        ) {}
    }

    @Test
    fun `root to gateway to 1 and 2`() {
        execute(
            source = "[Gateway](subdoc/gateway.qd)",
            outputResourceHook = {
                assertEquals(4, subdocumentGraph.vertices.size)
            },
        ) {}
    }

    @Test
    fun `circular, root to 1 to 2 to 1`() {
        execute(
            source = "[1](subdoc/circular-1.qd)",
            outputResourceHook = {
                assertEquals(3, subdocumentGraph.vertices.size)
            },
        ) {}
    }

    @Test
    fun `recursive, root to 1 recursively`() {
        execute(
            source = "[1](subdoc/recursive.qd)",
            outputResourceHook = {
                assertEquals(2, subdocumentGraph.vertices.size)
            },
        ) {}
    }
}
