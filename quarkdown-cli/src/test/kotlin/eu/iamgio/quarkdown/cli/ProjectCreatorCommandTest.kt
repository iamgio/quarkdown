package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import eu.iamgio.quarkdown.cli.creator.command.CreateProjectCommand
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

    private fun test(
        additionalArgs: Array<String> = emptyArray(),
        directory: File = super.directory,
    ) {
        command.test(
            directory.absolutePath,
            "--main-file",
            "main",
            "--name",
            "test",
            "--authors",
            "Aaa, Bbb,Ccc",
            "--type",
            "slides",
            "--lang",
            "en",
            "--color-theme",
            "darko",
            "--layout-theme",
            "latex",
            *additionalArgs,
        )
        assertTrue(directory.exists())

        println(directory.listFiles()!!.map { it.name })

        assertTrue("main.qmd" in directory.listFiles()!!.map { it.name })

        val main = directory.listFiles()!!.first { it.name == "main.qmd" }.readText()
        assertTrue(main.startsWith(".docname {test}"))
        assertTrue("- Aaa" in main)
        assertTrue("- Bbb" in main)
        assertTrue("- Ccc" in main)
        assertTrue(".doctype {slides}" in main)
        assertTrue(".doclang {English}" in main)
        assertTrue(".theme {darko} layout:{latex}" in main)
    }

    @Test
    fun default() {
        test()
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
}
