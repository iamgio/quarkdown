package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for scripting capabilities.
 */
class OptionalityTest {
    @Test
    fun `optional arguments`() {
        execute(
            """
            .function {greet}
                to?:
                Hello .to
            
            .greet {world}
            .greet
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello world</p>" +
                    "<p>Hello None</p>",
                it,
            )
        }

        execute(
            """
            .function {greet}
                to from?:
                Hello .to from .from
            
            .greet {world} {John}
            .greet {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello world from John</p>" +
                    "<p>Hello world from None</p>",
                it,
            )
        }

        execute(
            """
            .function {greet}
                to? from?:
                Hello .to from .from
            
            .greet {world} {John}
            .greet {world}
            .greet
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello world from John</p>" +
                    "<p>Hello world from None</p>" +
                    "<p>Hello None from None</p>",
                it,
            )
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(
                """
                .function {greet}
                    to? from:
                    Hello .to from .from
                
                .greet {world}
                """.trimIndent(),
            ) {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(
                """
                .function {greet}
                    to? from:
                    Hello .to from .from
                
                .greet
                """.trimIndent(),
            ) {}
        }

        execute(
            """
            .var {a} {0}
            
            .isnone {.a}
            
            .var {b} {.none}
            
            .isnone {.b}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><input disabled=\"\" type=\"checkbox\" /></p>" +
                    "<p><input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>",
                it,
            )
        }

        execute(
            """
            .function {greet}
                to from?:
                Hello .to from .from::otherwise {.to}
            
            .greet {world} {John}
            .greet {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello world from John</p>" +
                    "<p>Hello world from world</p>",
                it,
            )
        }
    }
}
