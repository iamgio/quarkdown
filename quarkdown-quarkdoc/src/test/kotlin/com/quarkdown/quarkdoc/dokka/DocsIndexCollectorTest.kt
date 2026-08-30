package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.quarkdoc.dokka.index.DocsIndexStorage
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [com.quarkdown.quarkdoc.dokka.index.DocsIndexCollectorTransformer]
 * collecting function data from the Dokka model.
 */
class DocsIndexCollectorTest :
    QuarkdocDokkaTest(
        imports = listOf(QuarkdownModule::class, VoidValue::class),
        stringImports =
            listOf(
                QuarkdownModule::class.java.packageName + ".*",
                LikelyChained::class.qualifiedName!!,
            ),
        stringPaths = listOf(LikelyChained::class.java.packageName + ".QuarkdocAnnotations"),
    ) {
    @Test
    fun `collects module functions from the model`() {
        test(
            mapOf(
                "M1.kt" to
                    """
                    object Module1 {
                        val Module: QuarkdownModule = moduleOf(this::greet)

                        /**
                         * Greets someone.
                         * @param who the *target* of the greeting
                         */
                        @LikelyChained
                        fun greet(who: String = "world") = VoidValue
                    }
                    """.trimIndent(),
            ),
            outModule = "Module1",
            outName = "greet",
        ) {
            val entry = DocsIndexStorage.find("Module1", "greet")
            assertNotNull(entry)

            val function = entry.function
            assertEquals("greet", function.name)
            assertTrue(function.isLikelyChained)

            val who = function.parameters.single()
            assertEquals("who", who.name)
            assertTrue(who.isOptional)
            assertContains(who.description, "*target*")
            // The property bullets appended by the parameter transformers are part of the description.
            assertContains(who.description, "Optional")
        }
    }
}
