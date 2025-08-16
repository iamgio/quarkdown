package com.quarkdown.quarkdoc.reader

import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for extracting content from Dokka HTML files.
 */
class DokkaReaderTest {
    @Test
    fun `html extractor`() {
        val fullHtml = javaClass.getResourceAsStream("/content/lowercase.html")!!.bufferedReader().readText()
        val extractedHtml = javaClass.getResourceAsStream("/extract/lowercase.html")!!.bufferedReader().readText()

        fun String.withoutWhitespace(): String = replace("\\s+".toRegex(), "")

        assertEquals(
            extractedHtml.withoutWhitespace(),
            DokkaHtmlContentExtractor(fullHtml)
                .extractContent()
                ?.withoutWhitespace(),
        )
    }

    private fun extractFunctionData(resourceName: String): DocsFunction {
        val fullHtml = javaClass.getResourceAsStream(resourceName)!!.bufferedReader().readText()
        return DokkaHtmlContentExtractor(fullHtml).extractFunctionData()!!
    }

    @Test
    fun `simple parameter extractor`() {
        val function = extractFunctionData("/content/lowercase.html")
        val parameter = function.parameters.first { it.name == "string" }
        assertEquals("lowercase", function.name)
        assertFalse(function.isLikelyChained)
        assertEquals(
            "<p class=\"paragraph\">string to convert</p>",
            parameter.description,
        )
        assertFalse(parameter.isOptional)
        assertFalse(parameter.isLikelyBody)
        assertFalse(parameter.isLikelyNamed)
    }

    @Test
    fun `long parameter extractor`() {
        val function = extractFunctionData("/content/container.html")
        val backgroundParameter = function.parameters.first { it.name == "background" }
        val bodyParameter = function.parameters.first { it.name == "body" }
        assertEquals("container", function.name)
        assertFalse(function.isLikelyChained)
        assertEquals(
            """
            <dl>
             <ul>
              <li>Optional</li>
              <li>Likely <a href>named</a></li>
             </ul>
            </dl>
            <p class="paragraph">background color. Transparent if unset</p>
            """.trimIndent(),
            backgroundParameter.description.replace("(?<=href)=\".+?\"".toRegex(), ""),
        )
        assertTrue(backgroundParameter.isOptional)
        assertFalse(backgroundParameter.isLikelyBody)
        assertTrue(backgroundParameter.isLikelyNamed)
        assertTrue(bodyParameter.isOptional)
        assertTrue(bodyParameter.isLikelyBody)
        assertFalse(bodyParameter.isLikelyNamed)
    }

    @Test
    fun `likely chained function extractor`() {
        val function = extractFunctionData("/content/isnone.html")
        assertEquals("isnone", function.name)
        assertTrue(function.isLikelyChained)
    }

    /**
     * Copies content/{capitalize.html, lowercase.html, uppercase.html, index.html} to a temp directory
     * in order to simulate the structure of a Dokka-generated module.
     * @param moduleName the name of the module to create as a subdirectory
     * @param resourceNames the names of the resources to copy into the module directory
     * @return the temporary directory
     */
    private fun copyResourcesToTempDir(
        moduleName: String,
        resourceNames: List<String>,
    ): File {
        val tempDir = createTempDirectory().toFile()
        val moduleDir = tempDir.resolve("com.quarkdown.stdlib.module.$moduleName").apply { mkdirs() }
        resourceNames.forEach { name ->
            javaClass.getResourceAsStream("/content/$name")!!.use { input ->
                moduleDir.resolve(name).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            assertTrue(moduleDir.resolve(name).exists())
        }
        return tempDir
    }

    @Test
    fun walker() {
        val rootDir = copyResourcesToTempDir("String", listOf("lowercase.html", "uppercase.html", "index.html"))
        val scanner = DokkaHtmlWalker(rootDir)
        val results = scanner.walk().toList().sortedBy { it.name }

        assertEquals(2, results.size)
        results[0].let {
            assertEquals("lowercase", it.name)
            assertEquals("String", it.moduleName)
            assertTrue(it.isInModule)
        }
        results[1].let {
            assertEquals("uppercase", it.name)
            assertEquals("String", it.moduleName)
            assertTrue(it.isInModule)
        }
    }
}
