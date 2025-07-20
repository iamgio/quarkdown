package com.quarkdown.test

import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for footnote definitions and references.
 */
class FootnoteTest {
    private fun referenceHtml(
        label: String,
        formattedIndex: String,
    ) = "<sup class=\"footnote-reference footnote-label\" data-definition=\"__footnote-$label\">" +
        "<a href=\"#__footnote-$label\">$formattedIndex</a>" +
        "</sup>"

    private fun definitionHtml(
        label: String,
        index: Int,
        formattedIndex: String = (index + 1).toString(),
        content: String,
    ) = "<span class=\"footnote-definition\" id=\"__footnote-$label\" data-footnote-index=\"$index\">" +
        "<sup class=\"footnote-label\">$formattedIndex</sup>" +
        "<span>$content</span>" +
        "</span>"

    @Test
    fun `fallback reference`() {
        execute(
            """
            Hello[^1]
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello[^1]</p>", it)
        }
    }

    @Test
    fun `reference after definition`() {
        execute(
            """
            Hello[^1]
            
            [^1]: This is a *footnote*.
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello" +
                    referenceHtml("1", "1") +
                    "</p>" +
                    definitionHtml(
                        label = "1",
                        index = 0,
                        content = "This is a <em>footnote</em>.",
                    ),
                it,
            )
        }
    }

    @Test
    fun `reference before definition`() {
        execute(
            """
            [^1]: This is a *footnote*.
            
            Hello[^1]
            """.trimIndent(),
        ) {
            assertEquals(
                definitionHtml(
                    label = "1",
                    index = 0,
                    content = "This is a <em>footnote</em>.",
                ) +
                    "<p>Hello" +
                    referenceHtml("1", "1") +
                    "</p>",
                it,
            )
        }
    }

    @Test
    fun `multiline definition`() {
        execute(
            """
            x[^long]
            
            [^long]: a multiline  
            note
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>x" +
                    referenceHtml("long", "1") +
                    "</p>" +
                    definitionHtml(
                        label = "long",
                        index = 0,
                        content = "a multiline<br />note",
                    ),
                it,
            )
        }
    }

    @Test
    fun `multiple references to same definition`() {
        execute(
            """
            x[^1] and y[^1]
            
            [^1]: note
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>x" +
                    referenceHtml("1", "1") +
                    " and y" +
                    referenceHtml("1", "1") +
                    "</p>" +
                    definitionHtml(
                        label = "1",
                        index = 0,
                        content = "note",
                    ),
                it,
            )
        }
    }

    @Test
    fun `single references to different definitions`() {
        execute(
            """
            x[^1] and y[^2] and z[^3]
            
            [^1]: note
            
            [^2]: another note
            
            [^3]: yet another note
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>x" +
                    referenceHtml("1", "1") +
                    " and y" +
                    referenceHtml("2", "2") +
                    " and z" +
                    referenceHtml("3", "3") +
                    "</p>" +
                    definitionHtml(
                        label = "1",
                        index = 0,
                        content = "note",
                    ) +
                    definitionHtml(
                        label = "2",
                        index = 1,
                        content = "another note",
                    ) +
                    definitionHtml(
                        label = "3",
                        index = 2,
                        content = "yet another note",
                    ),
                it,
            )
        }
    }

    @Test
    fun `multiple references to different definitions`() {
        execute(
            """
            a[^1] and b[^1] and c[^2] and d[^1] and e[^3] and f[^2]
            
            [^1]: note
            
            [^2]: another note
            
            [^3]: yet another note
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>a" +
                    referenceHtml("1", "1") +
                    " and b" +
                    referenceHtml("1", "1") +
                    " and c" +
                    referenceHtml("2", "2") +
                    " and d" +
                    referenceHtml("1", "1") +
                    " and e" +
                    referenceHtml("3", "3") +
                    " and f" +
                    referenceHtml("2", "2") +
                    "</p>" +
                    definitionHtml(
                        label = "1",
                        index = 0,
                        content = "note",
                    ) +
                    definitionHtml(
                        label = "2",
                        index = 1,
                        content = "another note",
                    ) +
                    definitionHtml(
                        label = "3",
                        index = 2,
                        content = "yet another note",
                    ),
                it,
            )
        }
    }

    @Test
    fun `all-in-one named definition and reference`() {
        execute("a[^x] and b[^x: first definition] and c[^y: second definition] and d[^x]") {
            assertEquals(
                "<p>a" +
                    referenceHtml("x", "1") +
                    " and b" +
                    referenceHtml("x", "1") +
                    definitionHtml(
                        label = "x",
                        index = 0,
                        content = "first definition",
                    ) +
                    " and c" +
                    referenceHtml("y", "2") +
                    definitionHtml(
                        label = "y",
                        index = 1,
                        content = "second definition",
                    ) +
                    " and d" +
                    referenceHtml("x", "1") +
                    "</p>",
                it,
            )
        }
    }

    @Test
    fun `all-in-one anonymous definition and reference`() {
        var uuid = 0
        val firstUuid = "2"
        val secondUuid = "4"
        execute(
            "a[^: anonymous definition] and b[^: *another* anonymous definition]",
            options = DEFAULT_OPTIONS.copy(uuidSupplier = { (++uuid * 2).toString() }),
        ) {
            assertEquals(
                "<p>a" +
                    referenceHtml(firstUuid, "1") +
                    definitionHtml(
                        label = firstUuid,
                        index = 0,
                        content = "anonymous definition",
                    ) +
                    " and b" +
                    referenceHtml(secondUuid, "2") +
                    definitionHtml(
                        label = secondUuid,
                        index = 1,
                        content = "<em>another</em> anonymous definition",
                    ) +
                    "</p>",
                it,
            )
        }
    }

    @Test
    fun numbered() {
        execute(
            """
            .numbering
              - footnotes: i
              
            a[^x] and b[^y]
            
            [^x]: note x
            
            [^y]: note y
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>a" +
                    referenceHtml("x", "i") +
                    " and b" +
                    referenceHtml("y", "ii") +
                    "</p>" +
                    definitionHtml(
                        label = "x",
                        index = 0,
                        formattedIndex = "i",
                        content = "note x",
                    ) +
                    definitionHtml(
                        label = "y",
                        index = 1,
                        formattedIndex = "ii",
                        content = "note y",
                    ),
                it,
            )
        }
    }
}
