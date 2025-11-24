package com.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.creator.command.CreateProjectCommand
import org.junit.Test
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [CreateProjectCommand]
 */
class ProjectCreatorCommandTest : TempDirectory() {
    private val command = CreateProjectCommand()

    @BeforeTest
    fun setup() {
        super.reset()
    }

    /**
     * Runs the command with the given arguments and verifies the created files.
     * @param additionalArgs additional command line arguments to pass
     * @param fixedMainFileName whether to use a fixed main file name
     * @param directory directory to create the project in
     * @param includeDescription whether to include the description argument
     * @param includeKeywords whether to include the keywords argument
     * @return content of the main file
     */
    private fun test(
        additionalArgs: Array<String> = emptyArray(),
        fixedMainFileName: Boolean = true,
        directory: File = super.directory,
        includeDescription: Boolean = true,
        includeKeywords: Boolean = true,
    ): String {
        // resolve(".") tests the canonical path instead of the actual one.
        command.test(
            directory.resolve(".").absolutePath,
            "--name",
            "test",
            "--authors",
            "Aaa, Bbb,Ccc",
            "--type",
            "slides",
            "--description",
            if (includeDescription) "A test document for slides" else "",
            "--lang",
            "en",
            "--color-theme",
            "darko",
            "--layout-theme",
            "latex",
            *additionalArgs,
            *if (includeKeywords) arrayOf("--keywords", "testing,slides, quarkdown") else emptyArray(),
            *if (fixedMainFileName) arrayOf("--main-file", "main") else emptyArray(),
        )
        assertTrue(directory.exists())

        val mainFileName = (if (fixedMainFileName) "main" else directory.name) + ".qd"

        assertTrue(mainFileName in directory.listFiles()!!.map { it.name })

        val main = directory.listFiles()!!.first { it.name == mainFileName }.readText()
        assertTrue(main.startsWith(".docname {test}"))
        if (includeDescription) {
            assertTrue(".docdescription {A test document for slides}" in main)
        }
        if (includeKeywords) {
            assertTrue(".dockeywords\n  - testing\n  - slides\n  - quarkdown" in main)
        }
        assertTrue("- Aaa" in main)
        assertTrue("- Bbb" in main)
        assertTrue("- Ccc" in main)
        assertTrue(".doctype {slides}" in main)
        assertTrue(".doclang {English}" in main)
        assertTrue(".theme {darko} layout:{latex}" in main)

        return main
    }

    @Test
    fun default() {
        test()
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default with relative name`() {
        test(fixedMainFileName = false)
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default in new directory`() {
        val dir = File(super.directory, "subdir")
        test(directory = dir)
        assertEquals(2, dir.listFiles()!!.size)
    }

    @Test
    fun `default empty`() {
        test(arrayOf("--empty"))
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun `empty description`() {
        val main = test(includeDescription = false)
        assertTrue(".docdescription" !in main)
    }

    @Test
    fun `no keywords`() {
        val main = test(includeKeywords = false)
        assertTrue(".dockeywords" !in main)
    }
}
