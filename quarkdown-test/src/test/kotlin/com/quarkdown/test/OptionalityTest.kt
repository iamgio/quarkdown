package com.quarkdown.test

import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

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
              
            .x::getat {2}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            
            .x::getat {5}::ifpresent {@lambda .1::sum {3}}::otherwise {No}
            """.trimIndent(),
        ) {
            assertEquals("<p>23</p><p>No</p>", it)
        }
    }

    @Test
    fun `node as fallback`() {
        execute(".none::otherwise {.text {hi}}") {
            assertEquals("<span>hi</span>", it)
        }

        execute(".none::otherwise {a .text {hi}}") {
            assertEquals("<p>a <span>hi</span></p>", it)
        }

        execute(".none::otherwise {a .text {hi} b}") {
            assertEquals("<p>a <span>hi</span> b</p>", it)
        }

        execute(".none::otherwise {.text {hi} b}") {
            assertEquals("<p><span>hi</span> b</p>", it)
        }
    }

    @Test
    fun `none as null parameter in native function`() {
        execute(
            """
            .row gap:{.none}
                Test
            """.trimIndent(),
        ) {
            assertFalse("gap" in it)
        }
    }

    @Test
    fun `none as null parameter in custom function`() {
        execute(
            """
            .function {greet}
                name?:
                Hi! I am .name::otherwise {unnamed}
            
            .greet {.none}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hi! I am unnamed</p>",
                it,
            )
        }
    }

    @Test
    fun `none as null parameter via operation`() {
        execute(
            """
            .var {condition} {false}
            .var {gap} {1cm}
            .row gap:{.condition::takeif {.condition}}
               Test
            """.trimIndent(),
        ) {
            assertFalse("gap" in it)
        }
    }

    @Test
    fun `none as non-nullable parameter should fail typecheck`() {
        assertFails {
            execute(".sin {.none}") {}
        }
    }

    @Test
    fun `none in if as falsy`() {
        execute(
            """
            .var {x} {.none}
            .if {.x}
               Hi!
            """.trimIndent(),
        ) {
            assertEquals("", it)
        }
    }

    @Test
    fun `none in ifnot as falsy`() {
        execute(
            """
            .var {x} {.none}
            .ifnot {.x}
               Hi!
            """.trimIndent(),
        ) {
            assertEquals("<p>Hi!</p>", it)
        }
    }
}
