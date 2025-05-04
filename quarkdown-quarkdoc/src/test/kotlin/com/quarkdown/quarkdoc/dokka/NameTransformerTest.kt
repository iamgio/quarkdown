package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.reflect.annotation.Name
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for name transformation in Dokka via `@Name`.
 */
class NameTransformerTest : QuarkdocDokkaTest(imports = listOf(Name::class)) {
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
            assertFalse("(?<!pageIds=\"root::$rootPackage//)someFunction".toRegex() in it)
        }
    }

    @Test
    fun `parameter name transformation`() {
        test(
            """
            fun someFunction(@Name("newname") oldParam: String) = Unit
            """.trimIndent(),
            "some-function",
        ) {
            assertContains(it, "newname")
            assertFalse("oldParam" in it)
        }
    }

    @Test
    fun `function and parameter name transformation`() {
        test(
            """
            @Name("newfuncname")
            fun someFunction(@Name("newparam1") oldParam1: String, @Name("newparam2") oldParam2: String) = Unit
            """.trimIndent(),
            "newfuncname",
        ) {
            assertContains(it, "newfuncname")
            assertContains(it, "newparam1")
            assertContains(it, "newparam2")
        }
    }

    @Test
    fun `parameter name transformation with doc`() {
        test(
            """
            /**
            * @param oldParam the parameter
            */
            fun someFunction(@Name("newname") oldParam: String) = Unit
            """.trimIndent(),
            "some-function",
        ) {
            val parameters = getParametersTable(it)
            assertEquals("newname the parameter", parameters.text())
        }
    }

    @Test
    fun `parameter name transformation with reference`() {
        test(
            """
            /**
            * The parameter is [oldParam].
            */
            fun someFunction(@Name("newname") oldParam: String) = Unit
            """.trimIndent(),
            "some-function",
        ) {
            assertEquals("The parameter is newname.", getParagraph(it))
        }
    }

    @Test
    fun `see-also function with transformed name`() {
        test(
            """
            @Name("newname")
            fun someFunction() = Unit
            
            /**
            * @see someFunction
            */
            fun anotherFunction() = Unit
            """.trimIndent(),
            "another-function",
        ) {
            assertEquals("newname", getSeeAlsoTable(it).text())
        }
    }

    @Test
    fun `referenced function with transformed name`() {
        test(
            """
            @Name("newname")
            fun someFunction() = Unit
            
            /**
            * The function is [someFunction].
            */
            fun anotherFunction() = Unit
            """.trimIndent(),
            "another-function",
        ) {
            assertEquals("The function is newname.", getParagraph(it))
        }
    }
}
