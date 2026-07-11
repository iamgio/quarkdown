package com.quarkdown.test

import com.quarkdown.core.context.MutableContext
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

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
            assertTrue((this as MutableContext).isFunctionExtended("test"))
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
                name:
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
                    unknown:
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
                name greeting:
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
                    name firstunknown secondunknown:
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
                name:
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
                greeting:
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
                name lastname greeting:
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
                name? lastname? greeting?:
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
                .super::uppercase

            .extend {greet}
                [.super]

            .greet {world}
            """.trimIndent(),
        ) {
            assertEquals("<p>[HELLO, WORLD]</p>", it)
        }
    }

    @Test
    fun `super argument override, single param`() {
        execute(
            """
            .function {test}
                name:
                Hello, .name
            
            .extend {test}
                name:
                .super name:{World}
            
            .test {John}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, World</p>", it)
        }
    }

    @Test
    fun `super argument override, multiple partial params`() {
        execute(
            """
            .function {greet}
                greeting name:
                .greeting, .name!

            .extend {greet}
                name:
                .name, .super greeting:{Howdy}

            .greet {Hello} {world}
            """.trimIndent(),
        ) {
            assertEquals("<p>world, Howdy, world!</p>", it)
        }
    }

    @Test
    fun `super argument override, argument transformation`() {
        execute(
            """
            .function {test}
                name lastname greeting:
                .greeting, .name .lastname
            
            .extend {test}
                name greeting:
                .name, .super greeting:{.greeting::uppercase}
            
            .test {John} {Doe} {Hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>John, HELLO, John Doe</p>", it)
        }
    }

    @Test
    fun `conditional extension`() {
        execute(
            """
            .function {mysum}
                 a b:
                 .a::sum {.b}

            .extend {mysum} where:{a b: .a::sum {.b}::islower than:{10}}
                a:
                .a
            
            .mysum {10} {11} .mysum {4} {8} .mysum {3} {5}
            """.trimIndent(),
        ) {
            assertEquals("<p>21 12 3</p>", it)
        }
    }

    @Test
    fun `multiple conditional extensions`() {
        execute(
            """
            .function {mysum}
                 a b:
                 .a::sum {.b}

            .extend {mysum} where:{a: .a::islower than:{10}}
                a:
                .a
            
            .extend {mysum} where:{b: .b::islower than:{10}}
                b:
                .b
                
            .mysum {10} {11} .mysum {4} {12} .mysum {16} {5} .mysum {8} {3}
            """.trimIndent(),
        ) {
            assertEquals("<p>21 4 5 3</p>", it)
        }
    }

    @Test
    fun `conditional extension, partial args`() {
        execute(
            """
            .function {mysum}
                 a b:
                 .a::sum {.b}

            .extend {mysum} where:{a: .a::islower than:{10}}
                a:
                .a
            
            .mysum {10} {11} .mysum {4} {8} .mysum {3} {5}
            """.trimIndent(),
        ) {
            assertEquals("<p>21 4 3</p>", it)
        }
    }

    @Test
    fun `stdlib extension`() {
        execute(
            """
            .extend {lowercase}
                .super::uppercase
            
            .lowercase {hello}
            """.trimIndent(),
        ) {
            assertEquals("<p>HELLO</p>", it)
        }
    }

    @Test
    fun `stdlib extension with injected parameter`() {
        execute(
            """
            .extend {heading} where:{content: .content::equals {Hi}}
                content:
                .container
                    .super
                        .content::plaintext::uppercase
            
            .heading {Hi} depth:{2}
            
            .heading {Hello} depth:{2}
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><h2>HI</h2></div><h2>Hello</h2>",
                it,
            )
        }
    }
}
