package com.quarkdown.quarkdoc.reader

import com.quarkdown.quarkdoc.reader.json.DOCS_INDEX_FILE_NAME
import com.quarkdown.quarkdoc.reader.json.DocsIndexWalker
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for reading pre-extracted documentation indexes.
 */
class DocsIndexTest {
    private fun indexJson(
        name: String,
        moduleName: String,
    ): String =
        """{"functions":[{"name":"$name","moduleName":"$moduleName","function":{"name":"$name","parameters":""" +
            """[{"name":"param","description":"a **param**","isOptional":true,""" +
            """"isLikelyNamed":true,"isLikelyBody":false,"allowedValues":["a","b"]}],"isLikelyChained":true},""" +
            """"contentMarkdown":"Does $name."}]}"""

    /**
     * @return a documentation tree carrying one index per nested module directory
     */
    private fun docsTree(): File {
        val root = createTempDirectory().toFile()
        root
            .resolve("module-a")
            .apply { mkdirs() }
            .resolve(DOCS_INDEX_FILE_NAME)
            .writeText(indexJson("alpha", "A"))
        root
            .resolve("module-b")
            .apply { mkdirs() }
            .resolve(DOCS_INDEX_FILE_NAME)
            .writeText(indexJson("beta", "B"))
        return root
    }

    @Test
    fun `merges nested module indexes`() {
        val walker = DocsIndexWalker.fromDirectoryOrNull(docsTree())!!
        val results = walker.walk().toList().sortedBy { it.name }

        assertEquals(2, results.size)
        results[0].let {
            assertEquals("alpha", it.name)
            assertEquals("A", it.moduleName)
            assertTrue(it.isInModule)

            val extractor = it.extractor()
            val function = extractor.extractFunctionData()
            assertEquals("alpha", function.name)
            assertEquals("Does alpha.", extractor.extractContent())

            val parameter = function.parameters.single()
            assertEquals("a **param**", parameter.description)
            assertTrue(parameter.isOptional)
            assertEquals(listOf("a", "b"), parameter.allowedValues)
            assertTrue(function.isLikelyChained)
        }
        assertEquals("beta", results[1].name)
    }

    @Test
    fun `no index, no walker`() {
        assertNull(DocsIndexWalker.fromDirectoryOrNull(createTempDirectory().toFile()))
    }
}
