package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for dictionary literals, lookup via `.get`, iteration and nesting.
 */
class DictionaryTest {
    private val authors =
        """
        .docauthors
          - John
            - from: USA
          - Maria
            - from: Italy

        """.trimIndent()

    @Test
    fun `lookup via nested get calls`() {
        execute(
            authors +
                """
                .var {john} {.docauthors::get {John}}

                .john::get {from}
                """.trimIndent(),
        ) {
            assertEquals("<p>USA</p>", it)
        }
    }

    @Test
    fun `lookup via inline chained get calls`() {
        execute(
            authors +
                """
                .docauthors::get {John}::get {from}
                """.trimIndent(),
        ) {
            assertEquals("<p>USA</p>", it)
        }
    }

    @Test
    fun `iterate dictionary entries via first and second`() {
        execute(
            authors +
                """
                .foreach {.docauthors}
                  An author is .first {.1}, from .second {.1}::get {from}
                """.trimIndent(),
        ) {
            assertEquals(
                "<p>An author is John, from USA</p>" +
                    "<p>An author is Maria, from Italy</p>",
                it,
            )
        }
    }

    @Test
    fun `flat dictionary lookup by key`() {
        execute(
            """
            .var {x}
              - a: 1
              - b: 2
              - c: 3

            .x::get {b}
            """.trimIndent(),
        ) {
            assertEquals("<p>2</p>", it)
        }
    }

    @Test
    fun `missing key returns None`() {
        execute(
            """
            .var {x}
              - a: 1
              - b: 2
              - c: 3

            .x::get {d}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><span class=\"codespan-content\"><code>None</code></span></p>",
                it,
            )
        }
    }

    @Test
    fun `missing key falls back when orelse is provided`() {
        execute(
            """
            .var {x}
              - a: 1
              - b: 2

            .x::get {z} orelse:{fallback}
            """.trimIndent(),
        ) {
            assertEquals("<p>fallback</p>", it)
        }
    }

    @Test
    fun `nested dictionary lookup`() {
        execute(
            """
            .var {x}
              - a:
                - aa: 1
                - ab: 2
              - b:
                - ba: 3
                - bb: 4

            .x::get {b}::get {ba}
            """.trimIndent(),
        ) {
            assertEquals("<p>3</p>", it)
        }
    }

    @Test
    fun `iterate dictionary of dictionaries built with explicit dictionary form`() {
        execute(
            """
            .var {x}
                .dictionary
                    - a
                      - aa: 1
                      - ab: 2
                    - b
                      - ba: 3
                      - bb: 4

            .foreach {.x}
                .var {name} {.first {.1}}
                .var {dict} {.second {.1}}
                .var {key} {.concatenate {.name} {b}}
                .var {value} {.dict::get {.key}}

                .name, .value
            """.trimIndent(),
        ) {
            assertEquals("<p>a, 2</p><p>b, 4</p>", it)
        }
    }
}
