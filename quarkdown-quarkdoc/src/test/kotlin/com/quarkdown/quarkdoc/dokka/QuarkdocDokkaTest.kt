package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.quarkdoc.dokka.storage.RenamingsStorage
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import org.jetbrains.dokka.testApi.logger.TestLogger
import org.jetbrains.dokka.utilities.DokkaConsoleLogger
import org.jetbrains.dokka.utilities.LoggingLevel
import org.jsoup.Jsoup
import utils.TestOutputWriterPlugin
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.BeforeTest

private const val SOURCE_DIR = "src/main/kotlin"
private const val SOURCE_ROOT = "$SOURCE_DIR/test/Test.kt"

private val CORE_SOURCE_DIR = File("../quarkdown-core/src/main/kotlin").absolutePath

/**
 * @return [this] class to a path in the source tree
 */
private fun KClass<*>.path(parent: String = CORE_SOURCE_DIR): String {
    val packageName = this.java.`package`.name
    val path = packageName.replace(".", "/")
    val className = this.simpleName
    return "$parent/$path/$className.kt"
}

/**
 * Base class for Dokka-based Quarkdoc tests.
 * @param rootPackage the root package for the test source
 */
open class QuarkdocDokkaTest(
    protected val rootPackage: String = "test",
) : BaseAbstractTest(logger = TestLogger(DokkaConsoleLogger(LoggingLevel.WARN))) {
    private val imports = listOf(Name::class, Injected::class)

    @BeforeTest
    fun setUp() {
        RenamingsStorage.clear()
    }

    private fun createConfiguration() =
        dokkaConfiguration {
            sourceSets {
                sourceSet {
                    sourceRoots = listOf(SOURCE_ROOT) + imports.map { it.path() }
                }
            }
        }

    private fun createFullSource(rootSource: String): String =
        buildString {
            append("/").append(SOURCE_ROOT).append("\n")
            append("package ").append(rootPackage).append("\n")
            imports.forEach { append("import ").append(it.qualifiedName).append("\n") }
            append(rootSource)
        }

    /**
     * Tests the output of a given source file.
     *
     * @param source the source code to test
     * @param outName the name of the output file, without extension
     * @param block action to execute with the output content.
     */
    protected fun test(
        source: String,
        outName: String,
        block: (String) -> Unit,
    ) {
        val writerPlugin = TestOutputWriterPlugin()
        testInline(
            createFullSource(source).also { println(it) },
            createConfiguration(),
            pluginOverrides = listOf(QuarkdocDokkaPlugin(), writerPlugin),
        ) {
            renderingStage = { _, _ ->
                val content = writerPlugin.writer.contents.getValue("root/$rootPackage/$outName.html")
                block(content)
            }
        }
    }

    /**
     * @param html the HTML content to parse
     * @return the function signature as text
     * @throws IllegalStateException if the signature is not found
     */
    protected fun getSignature(html: String) =
        Jsoup
            .parse(html)
            .select(".content > .monospace")
            .firstOrNull()
            ?.text()
            ?: throw IllegalStateException("Signature not found")

    /**
     * @param html the HTML content to parse
     * @return the main documentation paragraph text
     * @throws IllegalStateException if the paragraph is not found
     */
    protected fun getParagraph(html: String) =
        Jsoup
            .parse(html)
            .select(".content > .paragraph")
            .firstOrNull()
            ?.text()
            ?: throw IllegalStateException("Paragraph not found")

    private fun getTable(
        html: String,
        name: String,
    ) = Jsoup
        .parse(html)
        .select("h4:contains($name)")
        .firstOrNull()
        ?.nextElementSibling()
        ?: throw IllegalStateException("Table $name not found")

    /**
     * @param html the HTML content to parse
     * @return the parameters table element
     * @throws IllegalStateException if the table is not found
     */
    protected fun getParametersTable(html: String) = getTable(html, "Parameters")

    /**
     * @param html the HTML content to parse
     * @return the see-also table element
     * @throws IllegalStateException if the table is not found
     */
    protected fun getSeeAlsoTable(html: String) = getTable(html, "See also")
}
