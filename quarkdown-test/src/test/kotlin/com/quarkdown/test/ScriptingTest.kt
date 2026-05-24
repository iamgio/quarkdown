package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * End-to-end scripting showcases that combine multiple scripting features at once
 * (loops, variables, defined functions, recursion) into recognizable algorithms.
 *
 * Tests for individual scripting features live in dedicated files:
 * [ConditionalTest], [LoopTest], [FunctionDefinitionTest], [VariableTest],
 * [DictionaryTest], [DestructuringTest], [LambdaTest], [MathFunctionsTest].
 */
class ScriptingTest {
    /**
     * Expected Fibonacci sequence table, shared by all four variants below.
     */
    private val expectedFibonacciTable =
        "<table><thead><tr>" +
            "<th align=\"center\"><formula>F_{0}</formula></th>" +
            "<th align=\"center\"><formula>F_{1}</formula></th>" +
            "<th align=\"center\"><formula>F_{2}</formula></th>" +
            "<th align=\"center\"><formula>F_{3}</formula></th>" +
            "<th align=\"center\"><formula>F_{4}</formula></th>" +
            "</tr></thead><tbody><tr>" +
            "<td align=\"center\">0</td>" +
            "<td align=\"center\">1</td>" +
            "<td align=\"center\">1</td>" +
            "<td align=\"center\">2</td>" +
            "<td align=\"center\">3</td>" +
            "</tr></tbody></table>"

    /**
     * Iterative Fibonacci sequence calculation.
     */
    @Test
    fun `fibonacci, iterative at top level`() {
        execute(
            """
            .var {t1} {0}
            .var {t2} {1}

            .table
                .foreach {0..4}
                    | $ F_\{.1} $ |
                    |:-------------:|
                    |      .t1      |
                    .var {tmp} {.sum {.t1} {.t2}}
                    .t1 {.t2}
                    .t2 {.tmp}
            """.trimIndent(),
        ) {
            assertEquals(expectedFibonacciTable, it)
        }
    }

    /**
     * Iterative Fibonacci, but wrapped inside a defined function.
     */
    @Test
    fun `fibonacci, iterative inside a defined function`() {
        execute(
            """
            .function {fib}
                n:
                .var {t1} {0}
                .var {t2} {1}

                .table
                    .repeat {.n}
                        | $ F_\{.subtract {.1} {1}} $ |
                        |:--------------------------:|
                        |             .t1            |
                        .var {tmp} {.sum {.t1} {.t2}}
                        .t1 {.t2}
                        .var {t2} {.tmp}

            .fib {5}
            """.trimIndent(),
        ) {
            assertEquals(expectedFibonacciTable, it)
        }
    }

    /**
     * Iterative Fibonacci using a helper function for each new table column.
     */
    @Test
    fun `fibonacci, iterative with per-column helper function`() {
        execute(
            """
            .var {t1} {0}
            .var {t2} {1}

            .function {newtablecolumn}
                n:
                |  $ F_\{.n} $  |
                |:-------------:|
                |      .t1      |

            .table
                .foreach {0..4}
                    .newtablecolumn {.1}
                    .var {tmp} {.sum {.t1} {.t2}}
                    .var {t1} {.t2}
                    .var {t2} {.tmp}
            """.trimIndent(),
        ) {
            assertEquals(expectedFibonacciTable, it)
        }
    }

    /**
     * Recursive Fibonacci calculation.
     */
    @Test
    fun `fibonacci, recursive`() {
        execute(
            """
            .function {fib}
                n:
                .if { .islower {.n} than:{2} }
                    .n
                .ifnot { .islower {.n} than:{2} }
                    .sum {
                        .fib { .subtract {.n} {1} }
                    } {
                        .fib { .subtract {.n} {2} }
                    }

            .table
                .foreach {0..4}
                    | $ F_\{.1} $  |
                    |:------------:|
                    | .fib {.1} |
            """.trimIndent(),
        ) {
            assertEquals(expectedFibonacciTable, it)
        }
    }
}
