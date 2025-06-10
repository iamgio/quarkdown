package com.quarkdown.interaction

import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NodeModule
import com.quarkdown.interaction.executable.NpmWrapper
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private fun npm() = NpmWrapper(NpmWrapper.defaultPath)

private fun node(workingDirectory: File) = NodeJsWrapper(NodeJsWrapper.defaultPath, workingDirectory)

/**
 * Tests for wrappers around Node.js and NPM.
 * @see NodeJsWrapper
 * @see NpmWrapper
 */
class NodeNpmWrapperTest {
    private data object PuppeteerNodeModule : NodeModule("puppeteer")

    private val directory: File =
        createTempDirectory()
            .toFile()

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdirs()
    }

    @Test
    fun `nodejs wrapper`() {
        val node = node(workingDirectory = directory)
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
        assumeTrue(npm().isValid)
    }

    @Test
    fun `nonexisting npm`() {
        val npm = NpmWrapper("quarkdown-npm-nonexisting-path")
        assertEquals(false, npm.isValid)
    }

    @Test
    fun `nonexisting module not installed`() {
        val npm = npm()
        val module = NodeModule("quarkdown-nonexisting-module-xyz")
        assumeTrue(npm.isValid)
        assertFalse(npm.isInstalled(module))
    }

    @Test
    fun `puppeteer not linked`() {
        val node = node(workingDirectory = directory)
        assumeTrue(node.isValid)
        assertEquals(false, node.isLinked(PuppeteerNodeModule))
    }

    /*
    @Test
    fun `install puppeteer`() {
        val node = node(workingDirectory = directory)
        val npm = npm()
        assumeTrue(node.isValid)
        assumeTrue(npm.isValid)
        npm.install(PuppeteerNodeModule)
        assertTrue(npm.isInstalled(PuppeteerNodeModule))
        assertFalse(node.isLinked(PuppeteerNodeModule))
        npm.link(node, PuppeteerNodeModule)
        assertTrue(node.isLinked(PuppeteerNodeModule))
    }
     */
}
