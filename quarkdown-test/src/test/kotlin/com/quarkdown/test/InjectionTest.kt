package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for language injection functions.
 */
class InjectionTest {
    @Test
    fun html() {
        execute(".html\n\tHello, <b>world</b>!") {
            assertEquals("Hello, <b>world</b>!", it)
        }
    }

    @Test
    fun `block html`() {
        execute(
            """
            Hello
            
            .html
                <p><sup>World</sup></p>
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p><p><sup>World</sup></p>",
                it,
            )
        }
    }

    @Test
    fun `inline html`() {
        execute("Hello, .html {<sup>World</sup>}") {
            assertEquals(
                "<p>Hello, <sup>World</sup></p>",
                it,
            )
        }
    }

    @Test
    fun css() {
        execute(
            """
            .css
                .class {
                    background: red;
                }
            """.trimIndent(),
        ) {
            assertEquals(
                "<style data-hidden=\"\">.class {\n    background: red;\n}</style>",
                it,
            )
        }
    }

    @Test
    fun `css properties`() {
        execute(
            """
            .cssproperties
                - background-color: blue
                - main-font-size: 16px
            """.trimIndent(),
        ) {
            assertEquals(
                "<style data-hidden=\"\">" +
                    ":root { --qd-background-color: blue !important; --qd-main-font-size: 16px !important; }" +
                    "</style>",
                it,
            )
        }
    }
}
