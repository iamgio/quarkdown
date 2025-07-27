package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.test.util.execute
import kotlin.test.Test

/**
 * Tests for subdocument generation.
 */
class SubdocumentTest {
    private val subdocument = Subdocument("subdoc1", reader = { "Subdoc1".reader() })

    @Test
    fun `root to subdocument`() {
        execute(
            source = "",
            subdocumentGraph = { it.addVertex(subdocument).addEdge(Subdocument.ROOT, subdocument) },
            outputResourceHook = {
                println(it)
                // TODO test the output dir
            },
        ) {}
    }
}
