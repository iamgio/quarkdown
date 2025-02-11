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

        execute(
            """
            .function {greet}
                to? from:
                Hello .to from .from
            
            .greet from:{John}
            
            .greet
                John
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello None from John</p>" +
                    "<p>Hello None from John</p>",
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

    @Test
    fun optionality() {
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
              name:
              Hi! I am .name::otherwise {unnamed}
            
            .greet {John}
            
            .greet {.none}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hi! I am John</p>" +
                    "<p>Hi! I am unnamed</p>",
                it,
            )
        }

        execute(
            """
            .var {num} {5}
            .num::takeif {@lambda x: .x::equals {5}}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>5</p>",
                it,
            )
        }

        execute(
            """
            .function {oddeven}
              num:
              .num::takeif {@lambda x: .x::iseven}::ifpresent {Even}::otherwise {Odd}
              
            .oddeven {5}
            
            .oddeven {4}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Odd</p>" +
                    "<p>Even</p>",
                it,
            )
        }

        execute(
            """
            .function {present}
              x:
              .x::ifpresent {@lambda Yes, .1 is present}::otherwise {Not present}
            
            .present {5}
            
            .present {.none}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Yes, 5 is present</p>" +
                    "<p>Not present</p>",
                it,
            )
        }
    }

    @Test
    fun fallback() {
        // Dictionary.
        execute(
            """
            .var {x}
              - a: 1
              - b: 2
              - c: 3
              
            .get {b} from:{.x}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            
            .get {d} from:{.x}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            """.trimIndent(),
        ) {
            assertEquals("<p>5</p><p>No</p>", it)
        }

        // Collection.
        execute(
            """
            .var {x}
              - 10
              - 20
              - 30
              
            .getat {2} from:{.x}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            
            .getat {5} from:{.x}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            """.trimIndent(),
        ) {
            assertEquals("<p>23</p><p>No</p>", it)
        }
    }
}
