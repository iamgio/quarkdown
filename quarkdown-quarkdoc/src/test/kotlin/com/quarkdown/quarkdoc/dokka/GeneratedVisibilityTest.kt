package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.value.VoidValue
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * The native library processor emits three declarations per function: a public wrapper, an internal
 * dynamic counterpart, and private descriptors. Only the wrapper may reach the published
 * documentation, which relies on Dokka documenting public declarations only.
 */
class GeneratedVisibilityTest :
    QuarkdocDokkaTest(
        imports = listOf(QuarkdownModule::class, VoidValue::class),
        stringImports = listOf(QuarkdownModule::class.java.packageName + ".*"),
    ) {
    @Test
    fun `only the public wrapper is documented`() {
        test(
            mapOf(
                "Logger.kt" to
                    """
                    object Logger {
                        private val `P_log_message`: Int = 0
                        private val `F_log`: Int = 0
                        val Module: QuarkdownModule = moduleOf()
                        public fun `log`(`message`: String): VoidValue = VoidValue
                        internal fun `log__dynamic`(`message__raw`: Any): VoidValue = VoidValue
                    }
                    """.trimIndent(),
            ),
            outModule = "Logger",
            outName = "index",
        ) {
            val text = getText(it)
            assertContains(text, "log")
            assertFalse(text.contains("log__dynamic"), "the dynamic counterpart must stay undocumented")
            assertFalse(text.contains("P_log_message"), "parameter descriptors must stay undocumented")
            assertFalse(text.contains("F_log"), "function objects must stay undocumented")
        }
    }
}
