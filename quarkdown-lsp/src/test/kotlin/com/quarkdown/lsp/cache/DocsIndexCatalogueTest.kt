package com.quarkdown.lsp.cache

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [CacheableFunctionCatalogue] loading from a pre-extracted documentation index.
 */
class DocsIndexCatalogueTest {
    private lateinit var docsDir: File

    @BeforeTest
    fun setup() {
        clearCatalogue()
        docsDir = createTempDirectory().toFile()
        // Raw JSON, also pinning the index wire format.
        docsDir.resolve("docs-index.json").writeText(
            """{"functions":[{"name":"greet","moduleName":"String","function":{"name":"greet","parameters":""" +
                """[{"name":"who","description":"the *target* of the greeting","isOptional":false,""" +
                """"isLikelyNamed":false,"isLikelyBody":false,"allowedValues":null}],"isLikelyChained":false},""" +
                """"contentMarkdown":"Greets **somebody**."}]}""",
        )
    }

    @AfterTest
    fun teardown() {
        clearCatalogue()
    }

    @Test
    fun `catalogue loads from the index, keeping markdown as-is`() {
        val function = CacheableFunctionCatalogue.getCatalogue(docsDir).single()
        assertEquals("greet", function.name)
        assertEquals("String", function.rawData.moduleName)
        // Index content is already Markdown: no conversion is applied.
        assertEquals("Greets **somebody**.", function.documentationMarkdown)
        assertEquals(
            "the *target* of the greeting",
            function.data.parameters
                .single()
                .description,
        )
    }

    private fun clearCatalogue() {
        val field = CacheableFunctionCatalogue::class.java.getDeclaredField("catalogue")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(CacheableFunctionCatalogue) as MutableMap<File, Set<DocumentedFunction>>).clear()
    }
}
