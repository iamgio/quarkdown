package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

/**
 * Tests for subdocument generation.
 */
class SubdocumentTest {
    private val subdocument = Subdocument("subdoc1", reader = { "Subdoc1".reader() })

    private fun getResource(
        group: OutputResource?,
        subdocument: Subdocument,
    ): TextOutputArtifact {
        val resources = getSubResources(group)
        val resource = resources.firstOrNull { it.name == subdocument.name } as? TextOutputArtifact
        assertNotNull(resource)
        return resource
    }

    @Test
    fun `root to subdocument`() {
        execute(
            source = "",
            subdocumentGraph = { it.addVertex(subdocument).addEdge(Subdocument.ROOT, subdocument) },
            outputResourceHook = { group ->
                val resource = getResource(group, subdocument)
                assertContains(resource.content, "<html>")
            },
        ) {}
    }

    @Test
    fun `context sharing to subdocument`() {
        execute(
            source = ".doctype {paged}",
            subdocumentGraph = { it.addVertex(subdocument).addEdge(Subdocument.ROOT, subdocument) },
            outputResourceHook = { group ->
                val resource = getResource(group, subdocument)
                assertContains(resource.content, "paged")
            },
        ) {}
    }
}
