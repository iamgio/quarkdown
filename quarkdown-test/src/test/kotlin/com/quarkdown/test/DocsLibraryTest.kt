package com.quarkdown.test

import com.quarkdown.core.context.subdocument.subdocumentGraph
import com.quarkdown.core.document.DocumentType
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the `docs` library, which
 */
class DocsLibraryTest {
    @Test
    fun `multi-page docs with docs library`() {
        val pageCount = 3
        execute(
            ".include {docs}",
            workingDirectory = File(DATA_FOLDER, "subdoc").resolve("docs"),
            loadableLibraries = setOf("docs"),
        ) {
            // Subdocuments: 3 pages + 1 root.
            assertEquals(pageCount + 1, subdocumentGraph.vertices.size)
            // The graph is fully connected (remember that this block is run for each subdocument).
            assertEquals(pageCount, subdocumentGraph.getNeighbors(subdocument).count())

            assertEquals(DocumentType.DOCS, documentInfo.type)
            assertEquals("en", documentInfo.locale?.code)
            assertEquals("Common description", documentInfo.description)
        }
    }
}
