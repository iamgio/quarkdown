package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NodeModule
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for wrappers around Node.js and NPM.
 */
class NodeNpmWrapperTest {
    private val directory: File =
        kotlin.io.path
            .createTempDirectory()
            .toFile()

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun `nodejs wrapper`() {
        val wrapper = NodeJsWrapper(workingDirectory = directory)
        assertTrue(wrapper.isValid)
        assertEquals("Hello, Quarkdown!\n", wrapper.eval("console.log('Hello, Quarkdown!')"))
        assertEquals(
            "Hello, Quarkdown!\n".repeat(2),
            wrapper.eval(
                """
                function hello() {
                    console.log('Hello, Quarkdown!');
                }
                hello();
                hello();
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `nonexisting nodejs`() {
        val wrapper = NodeJsWrapper("quarkdown-nodejs-nonexisting-path", directory)
        assertEquals(false, wrapper.isValid)
    }

    @Test
    fun `npm wrapper`() {
        val wrapper = NpmWrapper()
        assertTrue(wrapper.isValid)
    }

    @Test
    fun `nonexisting npm`() {
        val wrapper = NpmWrapper("quarkdown-npm-nonexisting-path")
        assertEquals(false, wrapper.isValid)
    }

    @Test
    fun `nonexisting module not installed`() {
        val npm = NpmWrapper()
        val module = NodeModule("quarkdown-nonexisting-module-xyz")
        assertFalse(npm.isInstalled(module))
    }

    @Test
    fun `puppeteer not linked`() {
        val wrapper = NodeJsWrapper(workingDirectory = directory)
        assertEquals(false, wrapper.isLinked(PuppeteerNodeModule))
    }

    @Test
    fun `install puppeteer`() {
        val node = NodeJsWrapper(workingDirectory = directory)
        val npm = NpmWrapper()
        npm.install(PuppeteerNodeModule)
        assertTrue(npm.isInstalled(PuppeteerNodeModule))
        assertFalse(node.isLinked(PuppeteerNodeModule))
        npm.link(node, PuppeteerNodeModule)
        assertTrue(node.isLinked(PuppeteerNodeModule))
    }
}
