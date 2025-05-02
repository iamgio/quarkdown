package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.Name
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import utils.TestOutputWriterPlugin
import kotlin.test.Test
import kotlin.test.assertContains

private const val SOURCE_DIR = "src/main/kotlin"
private const val SOURCE_ROOT = "$SOURCE_DIR/test/Test.kt"
private val NAME_ANNOTATION_SOURCE = classToSourcePath(Name::class.java)

private fun classToSourcePath(cls: Class<*>): String {
    val packageName = cls.`package`.name
    val className = cls.simpleName
    return "$SOURCE_DIR/${packageName.replace(".", "/")}/$className.kt"
}

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class NameTransformerTest : BaseAbstractTest() {
    private fun test(
        source: String,
        outName: String,
        block: (String) -> Unit,
    ) {
        val configuration =
            dokkaConfiguration {
                sourceSets {
                    sourceSet {
                        sourceRoots = listOf(NAME_ANNOTATION_SOURCE, SOURCE_ROOT)
                    }
                }
            }

        val fullSource =
            """
            /$NAME_ANNOTATION_SOURCE
            package ${Name::class.java.`package`.name}
            
            annotation class ${Name::class.simpleName}(val name: String)
            
            /$SOURCE_ROOT
            package test
            import ${Name::class.qualifiedName}
            
            """.trimIndent() + source

        println(fullSource)

        val writerPlugin = TestOutputWriterPlugin()
        testInline(
            fullSource,
            configuration,
            pluginOverrides = listOf(QuarkdocDokkaPlugin(), writerPlugin),
        ) {
            renderingStage = { _, _ ->
                val content = writerPlugin.writer.contents.getValue("root/test/$outName.html")
                block(content)
            }
        }
    }

    @Test
    fun `no name transformation`() {
        test(
            "fun someFunction() = Unit",
            "some-function",
        ) {
            assertContains(it, "someFunction")
        }
    }

    @Test
    fun `function name transformation`() {
        test(
            """
            @Name("newname")
            fun someFunction() = Unit
            """.trimIndent(),
            "newname",
        ) {
            assertContains(it, "newname")
            // assertFalse("someFunction" in it)
        }
    }
}
