package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for iterables.
 */
class IterableTest {
    private val letters =
        """
        .var {abc}
          - A
          - B
          - C
        
        
        """.trimIndent()

    private val numbers =
        """
        .var {nums}
          - 1
          - 2
          - 3
          - 4
        
        
        """.trimIndent()

    @Test
    fun iterate() {
        execute(
            letters +
                """
                .foreach {.abc}
                  .1
                """.trimIndent(),
        ) {
            assertEquals("<p>A</p><p>B</p><p>C</p>", it)
        }
    }

    @Test
    fun `iterate with transform`() {
        execute(
            letters +
                """
                .foreach {.abc}
                  .lowercase {.1}
                """.trimIndent(),
        ) {
            assertEquals("<p>a</p><p>b</p><p>c</p>", it)
        }
    }

    @Test
    fun `get at`() {
        execute("$letters.abc::getat {2}") {
            assertEquals("<p>B</p>", it)
        }
    }

    @Test
    fun `get at, out of bounds`() {
        execute("$letters.abc::getat {5}") {
            assertEquals("<p><span class=\"codespan-content\"><code>None</code></span></p>", it)
        }
    }

    @Test
    fun `get first`() {
        execute("$letters.first from:{.abc}") {
            assertEquals("<p>A</p>", it)
        }
    }

    @Test
    fun `get last`() {
        execute("$letters.last from:{.abc}") {
            assertEquals("<p>C</p>", it)
        }
    }

    @Test
    fun `get size`() {
        execute("$letters.size of:{.abc}") {
            assertEquals("<p>3</p>", it)
        }
    }

    @Test
    fun `iterate numbers`() {
        execute(
            numbers +
                """
                .foreach {.nums}
                  n:
                  .pow {.n} to:{2}
                """.trimIndent(),
        ) {
            assertEquals("<p>1</p><p>4</p><p>9</p><p>16</p>", it)
        }
    }

    @Test
    fun `sum of numbers`() {
        execute("$numbers.nums::sumall") {
            assertEquals("<p>10</p>", it)
        }
    }

    @Test
    fun `average of numbers`() {
        execute("$numbers.nums::average") {
            assertEquals("<p>2.5</p>", it)
        }
    }

    @Test
    fun distinct() {
        execute(
            """
            .var {abc}
              - A
              - B
              - A
              - A
              - B
              - C
            
            .abc::distinct::size
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>3</p>",
                it,
            )
        }
    }

    @Test
    fun reverse() {
        execute("$letters.foreach {.abc::reversed}\n\t.1") {
            assertEquals("<p>C</p><p>B</p><p>A</p>", it)
        }
    }

    @Test
    fun `sort by natural order`() {
        execute(
            """
            .var {abc}
              - C
              - A
              - B
            
            .foreach {.abc::sorted}
                .1
            """.trimIndent(),
        ) {
            assertEquals("<p>A</p><p>B</p><p>C</p>", it)
        }
    }

    @Test
    fun `sort by natural order, descending`() {
        execute(
            """
            .var {abc}
              - 3
              - 1
              - 2
            
            .foreach {.abc::sorted::reversed}
                .1
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p><p>2</p><p>1</p>", it)
        }
    }

    @Test
    fun `sort by property`() {
        execute(
            """
             .var {dict}
                 .dictionary
                     - a: 3
                     - b: 1
                     - c: 2
             
            .foreach {.dict::sorted by:{@lambda name value: .value}}
                name value:
                .name
            """.trimIndent(),
        ) {
            assertEquals("<p>b</p><p>c</p><p>a</p>", it)
        }
    }

    @Test
    fun group() {
        execute(
            """
            .var {abc}
              - A
              - B
              - A
              - A
              - B
              - C
            
            .foreach {.abc::groupvalues}
                group:
                Group of .group::first of size .group::size
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Group of A of size 3</p>" +
                    "<p>Group of B of size 2</p>" +
                    "<p>Group of C of size 1</p>",
                it,
            )
        }
    }

    @Test
    fun `handle pairs`() {
        execute(
            """
            .var {p} {.pair {1} {2}}
            .sum {.first {.p}} {.second {.p}}
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }
    }

    @Test
    fun `iterate pairs`() {
        execute(
            """
            .foreach {.pair {1} {2}}
              .1
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p><p>2</p>", it)
        }
    }
}
