package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.index.DokkaHtmlContentExtractor
import com.quarkdown.quarkdoc.dokka.index.DokkaHtmlWalker
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for reading Dokka-generated HTML output, used to join rendered content into the docs index.
 */
class DokkaHtmlReadingTest {
    @Test
    fun `html content extractor`() {
        val fullHtml = javaClass.getResourceAsStream("/content/lowercase.html")!!.bufferedReader().readText()
        val extractedHtml = javaClass.getResourceAsStream("/extract/lowercase.html")!!.bufferedReader().readText()

        fun String.withoutWhitespace(): String = replace("\\s+".toRegex(), "")

        val actual = DokkaHtmlContentExtractor(fullHtml).extractContent()!!

        assertEquals(
            extractedHtml.withoutWhitespace(),
            actual.withoutWhitespace(),
        )
        // Code-block copy buttons must be stripped from the extracted content.
        assertFalse("copy-tooltip" in actual)
        assertFalse(actual.contains("clipboard", ignoreCase = true))
    }

    /**
     * @return a temporary directory mirroring a Dokka module structure with the given resources
     */
    private fun copyResourcesToTempDir(
        moduleName: String,
        resourceNames: List<String>,
    ): File {
        val tempDir = createTempDirectory().toFile()
        val moduleDir = tempDir.resolve("com.quarkdown.stdlib.module.$moduleName").apply { mkdirs() }
        resourceNames.forEach { name ->
            javaClass.getResourceAsStream("/content/$name")!!.use { input ->
                moduleDir.resolve(name).outputStream().use(input::copyTo)
            }
        }
        return tempDir
    }

    @Test
    fun walker() {
        val rootDir = copyResourcesToTempDir("String", listOf("lowercase.html", "uppercase.html", "index.html"))
        val results = DokkaHtmlWalker(rootDir).walk().toList().sortedBy { it.name }

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
