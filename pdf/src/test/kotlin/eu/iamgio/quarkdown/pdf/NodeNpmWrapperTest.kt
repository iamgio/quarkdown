package eu.iamgio.quarkdown.pdf

import eu.iamgio.quarkdown.pdf.html.executable.NodeJsWrapper
import eu.iamgio.quarkdown.pdf.html.executable.NodeModule
import eu.iamgio.quarkdown.pdf.html.executable.NpmWrapper
import eu.iamgio.quarkdown.pdf.html.executable.PuppeteerNodeModule
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for wrappers around Node.js and NPM.
 * @see NodeJsWrapper
 * @see NpmWrapper
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
        val node = NodeJsWrapper(workingDirectory = directory)
        assumeTrue(node.isValid)

        assertEquals("Hello, Quarkdown!", node.eval("console.log('Hello, Quarkdown!')"))
        assertEquals(
            "Hello, Quarkdown!\nHello, Quarkdown!",
            node.eval(
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
        val node = NodeJsWrapper("quarkdown-nodejs-nonexisting-path", directory)
        assertEquals(false, node.isValid)
    }

    @Test
    fun `npm wrapper`() {
        val npm = NpmWrapper()
        assumeTrue(npm.isValid)
    }

    @Test
    fun `nonexisting npm`() {
        val npm = NpmWrapper("quarkdown-npm-nonexisting-path")
        assertEquals(false, npm.isValid)
    }

    @Test
    fun `nonexisting module not installed`() {
        val npm = NpmWrapper()
        val module = NodeModule("quarkdown-nonexisting-module-xyz")
        assumeTrue(npm.isValid)
        assertFalse(npm.isInstalled(module))
    }

    @Test
    fun `puppeteer not linked`() {
        val node = NodeJsWrapper(workingDirectory = directory)
        assumeTrue(node.isValid)
        assertEquals(false, node.isLinked(PuppeteerNodeModule))
    }

    @Test
    fun `install puppeteer`() {
        val node = NodeJsWrapper(workingDirectory = directory)
        val npm = NpmWrapper()
        assumeTrue(node.isValid)
        assumeTrue(npm.isValid)
        npm.install(PuppeteerNodeModule)
        assertTrue(npm.isInstalled(PuppeteerNodeModule))
        assertFalse(node.isLinked(PuppeteerNodeModule))
        npm.link(node, PuppeteerNodeModule)
        assertTrue(node.isLinked(PuppeteerNodeModule))
    }
}
