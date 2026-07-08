package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.quarkdoc.dokka.transformers.module.QuarkdownModulesStorage
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Tests for synthetic module creation in Dokka.
 */
class ModuleTransformerTest :
    QuarkdocDokkaTest(
        imports = listOf(QuarkdownModule::class, VoidValue::class),
        stringImports = listOf(QuarkdownModule::class.java.packageName + ".*"),
    ) {
    @Test
    fun `two modules`() {
        val sources =
            mapOf(
                "M1.kt" to
                    """
                    object Module1 {
                        val Module: QuarkdownModule = moduleOf(this::aFunction)
                        fun aFunction() = VoidValue
                    }
                    """.trimIndent(),
                "M2.kt" to
                    """
                    object Module2 {
                        val Module: QuarkdownModule = moduleOf(this::bFunction)
                        fun bFunction() = VoidValue
                    }
                    """.trimIndent(),
            )

        test(
            sources,
            outModule = "Module1",
            outName = "a-function",
        ) {
            assertEquals(2, QuarkdownModulesStorage.moduleCount)
            assertContains(getSignature(it), "aFunction")
        }
    }

    @Test
    fun `two modules and leftover file`() {
        val sources =
            mapOf(
                "M1.kt" to
                    """
                    object Module1 {
                        val Module: QuarkdownModule = moduleOf(this::aFunction)
                        fun aFunction() = VoidValue
                    }
                    """.trimIndent(),
                "M2.kt" to
                    """
                    object Module2 {
                        val Module: QuarkdownModule = moduleOf(this::bFunction)
                        fun bFunction() = VoidValue
                    }
                    """.trimIndent(),
                "leftover.kt" to "object leftover {}",
            )

        test(
            sources,
            outName = "leftover/index",
        ) {
            // No error = file exists
        }
    }

    @Test
    fun `five modules, package list`() {
        val moduleCount = 5
        val sources =
            (1..moduleCount).associate {
                "M$it.kt" to
                    """
                    object Module$it {
                        val Module: QuarkdownModule = moduleOf(this::someFunction$it)
                        fun someFunction$it() = VoidValue
                    }
                    """.trimIndent()
            }

        test(
            sources,
            outName = "root/package-list",
            autoPath = false,
        ) {
            assertEquals(moduleCount, QuarkdownModulesStorage.moduleCount)
            assertContains(it, rootPackage)
            for (i in 1..moduleCount) {
                assertContains(it, "$rootPackage.module.Module$i")
                assertContains(it, "$rootPackage.module.Module$i/some-function$i.html")
            }
        }
    }
}
