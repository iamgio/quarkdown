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
                "<style data-hidden=\"\">.class {\n    background: red !important;\n}</style>",
                it,
            )
        }
    }

    @Test
    fun `css from file`() {
        execute(".css {.read {css/style.css}}") {
            assertEquals(
                "<style data-hidden=\"\">body {\n    background-color: orange !important;\n}</style>",
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

    @Test
    fun `css with custom class elements`() {
        execute(
            """
            .container classname:{my-custom}
                Content
                
            Hi .text {content} classname:{my-custom}
            
            .css
                .my-custom {
                    color: red;
                }
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container my-custom\"><p>Content</p></div>" +
                    "<p>Hi <span class=\"my-custom\">content</span></p>" +
                    "<style data-hidden=\"\">.my-custom {\n    color: red !important;\n}</style>",
                it,
            )
        }
    }
}
