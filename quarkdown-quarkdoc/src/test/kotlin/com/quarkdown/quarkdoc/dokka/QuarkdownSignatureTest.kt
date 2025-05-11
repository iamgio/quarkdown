package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.VoidValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class QuarkdownSignatureTest :
    QuarkdocDokkaTest(
        imports = listOf(MultiFunctionLibraryLoader::class, VoidValue::class, DynamicValue::class),
        stringImports = listOf("${MultiFunctionLibraryLoader::class.java.`package`.name}.Module"),
    ) {
    /**
     * @param functionCode the code of the function to test. Its name must be equal to [functionName]
     * @param functionName the name of the function to test
     * @param block the block to execute with the signature as a parameter
     */
    private fun testSignature(
        functionCode: String,
        functionName: String = "func",
        block: (String) -> Unit,
    ) {
        val sources =
            mapOf(
                "TestModule.kt" to
                    """
                    val TestModule: Module = moduleOf(::$functionName)
                    $functionCode
                    """.trimIndent(),
            )

        test(
            sources,
            outModule = "TestModule",
            outName = functionName,
        ) { block(getSignature(it)) }
    }

    @Test
    fun `no parameters`() {
        testSignature("fun func() = VoidValue") {
            assertEquals(".func -> Void", it)
        }
    }

    @Test
    fun `one parameter`() {
        testSignature("fun func(a: Int) = VoidValue") {
            assertEquals(".func {a: Int} -> Void", it)
        }
    }

    @Test
    fun `two parameters`() {
        testSignature("fun func(a: Int, b: Iterable<DynamicValue>) = VoidValue") {
            assertEquals(".func {a: Int} {b: Iterable<Dynamic>} -> Void", it)
        }
    }

    @Test
    fun `default value`() {
        testSignature("fun func(a: Int = 0) = VoidValue") {
            assertEquals(".func {a: Int = 0} -> Void", it)
        }
    }
}
