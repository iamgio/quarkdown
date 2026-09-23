package com.quarkdown.stdlib.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class CssTest {
    @Test
    fun `single property`() {
        assertEquals(
            "body { color: red !important; }",
            applyImportantToCSS("body { color: red; }"),
        )
    }

    @Test
    fun `multiple properties`() {
        assertEquals(
            "body { color: red !important; background: blue !important; }",
            applyImportantToCSS("body { color: red; background: blue; }"),
        )
    }

    @Test
    fun `already has important`() {
        assertEquals(
            "body { color: red !important; }",
            applyImportantToCSS("body { color: red !important; }"),
        )
    }

    @Test
    fun `mixed with and without important`() {
        assertEquals(
            "body { color: red !important; background: blue !important; }",
            applyImportantToCSS("body { color: red !important; background: blue; }"),
        )
    }

    @Test
    fun `multiline css`() {
        val input =
            """
            body {
                color: red;
                background: blue;
            }
            """.trimIndent()
        val expected =
            """
            body {
                color: red !important;
                background: blue !important;
            }
            """.trimIndent()
        assertEquals(expected, applyImportantToCSS(input))
    }

    @Test
    fun `multiple selectors`() {
        val input = "h1 { font-size: 2em; } p { margin: 0; }"
        val expected = "h1 { font-size: 2em !important; } p { margin: 0 !important; }"
        assertEquals(expected, applyImportantToCSS(input))
    }

    @Test
    fun `complex values`() {
        assertEquals(
            "body { font-family: Arial, sans-serif !important; }",
            applyImportantToCSS("body { font-family: Arial, sans-serif; }"),
        )
    }

    @Test
    fun `css variables`() {
        assertEquals(
            ":root { --qd-color: red !important; }",
            applyImportantToCSS(":root { --qd-color: red; }"),
        )
    }

    @Test
    fun `value with url`() {
        assertEquals(
            "body { background: url('image.png') !important; }",
            applyImportantToCSS("body { background: url('image.png'); }"),
        )
    }

    @Test
    fun `multiline without trailing semicolon`() {
        val input =
            """
            body {
                color: red;
                background: blue
            }
            """.trimIndent()
        val expected =
            """
            body {
                color: red !important;
                background: blue !important
            }
            """.trimIndent()
        assertEquals(expected, applyImportantToCSS(input))
    }
}
