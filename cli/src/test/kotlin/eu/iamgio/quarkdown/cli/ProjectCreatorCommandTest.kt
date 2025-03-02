package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import eu.iamgio.quarkdown.cli.creator.command.CreateProjectCommand
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 *
 */
class ProjectCreatorCommandTest {
    private val directory =
        kotlin.io.path
            .createTempDirectory()
            .toFile()

    private val command = CreateProjectCommand()

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun default() {
        command.test("$directory --name test")
        assertEquals(2, directory.listFiles()!!.size)
    }

    @Test
    fun `default empty`() {
        command.test("$directory --empty --name test")
        assertEquals(1, directory.listFiles()!!.size)
        assertEquals("main.qmd", directory.listFiles()!!.single().name)
    }
}
