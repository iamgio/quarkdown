package com.quarkdown.quarkdoc.reader

import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for extracting content from Dokka HTML files.
 */
class DokkaReaderTest {
    @Test
    fun `html extractor`() {
        val subContentRange = 114..145
        val fullHtml = javaClass.getResourceAsStream("/content/lowercase.html")!!.bufferedReader().readText()
        assertEquals(
            fullHtml
                .lines()
                .subList(subContentRange.first, subContentRange.last)
                .joinToString("\n")
                .replace("\\s+".toRegex(), ""),
            DokkaHtmlContentExtractor(fullHtml)
                .extractContent()
                ?.replace("\\s+".toRegex(), ""),
        )
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
        val moduleDir = tempDir.resolve("com/quarkdown/stdlib/module/$moduleName").apply { mkdirs() }
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
        }
        results[1].let {
            assertEquals("uppercase", it.name)
            assertEquals("String", it.moduleName)
        }
    }
}
