package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import eu.iamgio.quarkdown.cli.creator.CreateProjectCommand
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

/**
 *
 */
class ProjectCreatorCommandTest {
    private val rootDirectory =
        kotlin.io.path
            .createTempDirectory()
            .toFile()

    @Test
    fun default() {
        val command = CreateProjectCommand()
        val directory = File(rootDirectory, "default")
        command.test("$directory")
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default empty`() {
        val command = CreateProjectCommand()
        val directory = File(rootDirectory, "empty")
        command.test("$directory --empty")
        assertEquals(1, directory.listFiles()!!.size)
        assertEquals("main.qmd", directory.listFiles()!!.single().name)
    }
}
