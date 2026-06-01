package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Tests for function extension via `.extend`.
 */
class FunctionExtensionTest {
    @Test
    fun `no params, no super access`() {
        execute(
            """
            .function {test}
               Hello
            
            .extend {test}
               Hi
            
            .test
            """.trimIndent(),
            forbidFunctionOverwriting = true,
        ) {
            assertEquals("<p>Hi</p>", it)
        }
    }

    @Test
    fun `unknown function throws`() {
        assertFails {
            execute(
                """
                .extend {unknown}
                   Hi
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `no params, super access`() {
        execute(
            """
            .function {test}
               Hello
            
            .extend {test}
               super:
               .super::uppercase
            
            .test
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO</p>", it)
        }
    }

    @Test
    fun `single param, super access`() {
        execute(
            """
            .function {test}
                name:
                Hello, .name
            
            .extend {test}
                super name:
                .if {.name::equals {World}}
                    .super::uppercase
                .ifnot {.name::equals {World}}
                    .super
            
            .test {World}
            
            .test {Everyone}
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO, WORLD</p><p>Hello, Everyone</p>", it)
        }
    }

    @Test
    fun `unknown single param throws`() {
        assertFails {
            execute(
                """
                .function {test}
                    name:
                    Hello, .name
                
                .extend {test}
                    super unknown:
                    .super
                
                .test {World}
                """.trimIndent(),
            ) {}
        }.also {
            assertContains(it.message!!, "unknown")
        }
    }

    @Test
    fun `implicit single param, super access`() {
        execute(
            """
            .function {test}
                name:
                Hello, .name
            
            .extend {test}
                super:
                .super::uppercase
            
            .test {World}
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO, WORLD</p>", it)
        }
    }

    @Test
    fun `multiple explicit params, super access`() {
        execute(
            """
            .function {test}
                name greeting:
                .greeting, .name
            
            .extend {test}
                super name greeting:
                .greeting .super .name
            
            .test {World} {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello Hello, World World</p>", it)
        }
    }

    @Test
    fun `unknown multiple params throw`() {
        assertFails {
            execute(
                """
                .function {test}
                    name lastname:
                    Hello, .name .lastname
                
                .extend {test}
                    super name firstunknown secondunknown:
                    .super
                
                .test {World}
                """.trimIndent(),
            ) {}
        }.also {
            assertContains(it.message!!, "firstunknown, secondunknown")
        }
    }

    @Test
    fun `multiple implicit params, super access`() {
        execute(
            """
            .function {test}
                name lastname greeting:
                .greeting, .name .lastname
            
            .extend {test}
                super:
                .super!
            
            .test {John} {Doe} {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, John Doe!</p>", it)
        }
    }

    @Test
    fun `multiple partially implicit params, super access`() {
        execute(
            """
            .function {test}
                name lastname greeting:
                .greeting, .name .lastname
            
            .extend {test}
                super name:
                .name, .super!
            
            .test {John} {Doe} {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>John, Hello, John Doe!</p>", it)
        }
    }

    @Test
    fun `multiple partially implicit params by name, super access`() {
        execute(
            """
            .function {test}
                name lastname greeting:
                .greeting, .name .lastname

            .extend {test}
                super greeting:
                .greeting, .super!

            .test {John} {Doe} {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, Hello, John Doe!</p>", it)
        }
    }

    @Test
    fun `shuffled named arguments preserve binding`() {
        execute(
            """
            .function {test}
                name lastname greeting:
                .greeting, .name .lastname

            .extend {test}
                super name lastname greeting:
                .greeting .name .lastname: .super

            .test greeting:{Hi} name:{John} lastname:{Doe}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hi John Doe: Hi, John Doe</p>", it)
        }
    }

    @Test
    fun `sparse named arguments preserve binding`() {
        execute(
            """
            .function {test}
                name? lastname? greeting?:
                .greeting::otherwise {Hello}, .name::otherwise {?} .lastname::otherwise {?}

            .extend {test}
                super name? lastname? greeting?:
                .greeting::otherwise {none}/.name::otherwise {none}/.lastname::otherwise {none}: .super

            .test name:{John} greeting:{Hi}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hi/John/none: Hi, John ?</p>", it)
        }
    }

    @Test
    fun `chained extensions wrap previous wrapper`() {
        execute(
            """
            .function {greet}
                name:
                Hello, .name

            .extend {greet}
                super:
                .super::uppercase

            .extend {greet}
                super:
                [.super]

            .greet {world}
            """.trimIndent(),
        ) {
            assertEquals("<p>[HELLO, WORLD]</p>", it)
        }
    }

    @Test
    fun `super access via implicit parameter`() {
        execute(
            """
            .function {test}
                name:
                Hello, .name

            .extend {test}
                .1::uppercase

            .test {World}
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO, WORLD</p>", it)
        }
    }

    @Test
    fun `stdlib extension`() {
        execute(
            """
            .extend {heading}
                super content:
                .if {.content::equals {Hi}}
                    .container
                        .super
                .ifnot {.content::equals {Hi}}
                    .super
            
            .heading {Hi} depth:{2}
            
            .heading {Hello} depth:{2}
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><h2>Hi</h2></div><h2>Hello</h2>",
                it,
            )
        }
    }
}
