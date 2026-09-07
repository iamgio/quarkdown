package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for text formatting.
 */
class TextTest {
    @Test
    fun `simple text`() {
        execute("Hello, world!") {
            assertEquals("<p>Hello, world!</p>", it)
        }
    }

    @Test
    fun paragraphs() {
        execute(
            """
            First paragraph.
            
            Second paragraph.
            
            Third paragraph.
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>First paragraph.</p><p>Second paragraph.</p><p>Third paragraph.</p>",
                it,
            )
        }
    }

    @Test
    fun `standard line break`() {
        execute("This is a line break.  \nThis is the next line.") {
            assertEquals("<p>This is a line break.<br />This is the next line.</p>", it)
        }
    }

    @Test
    fun `line break function`() {
        execute("This is a line break. .br This is the next line.") {
            assertEquals("<p>This is a line break. <br /> This is the next line.</p>", it)
        }
    }

    @Test
    fun `formatted text`() {
        execute(
            """
            This is *some* **text**. This _is_ __great__!
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>This is <em>some</em> <strong>text</strong>. This <em>is</em> <strong>great</strong>!</p>",
                it,
            )
        }
    }

    @Test
    fun `text replacement`() {
        execute(
            """
            > This is a **"quote"** with 'text *replacement*'.  
            > This is a feature of Quarkdown--the Turing complete Markdown--by iamgio (C) 2024 - all rights reserved.
            > => Quarkdown != other Markdown flavors... <-
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote><p>" +
                    "This is a <strong>&ldquo;quote&rdquo;</strong> with &lsquo;text <em>replacement</em>&rsquo;.<br />" +
                    "This is a feature of Quarkdown&mdash;the Turing complete Markdown&mdash;by iamgio &copy; 2024 " +
                    "&ndash; all rights reserved. " +
                    "&rArr; Quarkdown &ne; other Markdown flavors&hellip; &larr;" +
                    "</p></blockquote>",
                it,
            )
        }
    }

    @Test
    fun `text in headings`() {
        execute("# Title\n Hello, world!\n## Subtitle\nHello, world!") {
            assertEquals(
                "<h1>Title</h1><p>Hello, world!</p><h2>Subtitle</h2><p>Hello, world!</p>",
                it,
            )
        }
    }

    @Test
    fun links() {
        execute("Hello, **world**! [_link_](https://example.com \"title\")") {
            assertEquals(
                "<p>Hello, <strong>world</strong>! <a href=\"https://example.com\" title=\"title\"><em>link</em></a></p>",
                it,
            )
        }
    }

    @Test
    fun `advanced text formatting`() {
        execute("This is a .text {small text} size:{tiny} variant:{smallcaps}") {
            assertEquals(
                "<p>This is a <span style=\"font-size: var(--qd-size-tiny, 1em); font-variant: small-caps;\">small text</span></p>",
                it,
            )
        }
    }

    @Test
    fun `block text is wrapped in a paragraph`() {
        execute(".text {some text} size:{tiny}") {
            assertEquals(
                "<p><span style=\"font-size: var(--qd-size-tiny, 1em);\">some text</span></p>",
                it,
            )
        }
    }

    @Test
    fun `text with body argument`() {
        execute(
            """
            .text size:{tiny} variant:{smallcaps}
                small text
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><span style=\"font-size: var(--qd-size-tiny, 1em); font-variant: small-caps;\">small text</span></p>",
                it,
            )
        }
    }

    @Test
    fun `subscript and superscript`() {
        execute("H{.text {2} script:{sub}}O is water. E = mc{.text {2} script:{sup}}") {
            assertEquals(
                "<p>H<sub>2</sub>O is water. E = mc<sup>2</sup></p>",
                it,
            )
        }
    }

    @Test
    fun `simple whitespace`() {
        execute(
            """
            Line 1
            
            .whitespace
            
            Line 2 after a long break
            """.trimIndent(),
        ) {
            assertEquals("<p>Line 1</p><div class=\"whitespace\">&nbsp;</div><p>Line 2 after a long break</p>", it)
        }
    }

    @Test
    fun `inline whitespace`() {
        execute("A .whitespace B") {
            assertEquals("<p>A <span class=\"whitespace\">&nbsp;</span> B</p>", it)
        }
    }

    @Test
    fun `sized inline whitespace`() {
        execute("A .whitespace width:{1cm} B") {
            assertEquals("<p>A <span class=\"whitespace\" style=\"width: 1.0cm;\"></span> B</p>", it)
        }

        execute("A .whitespace width:{1cm} height:{3mm} B") {
            assertEquals("<p>A <span class=\"whitespace\" style=\"width: 1.0cm; height: 3.0mm;\"></span> B</p>", it)
        }
    }

    @Test
    fun `sized block whitespace`() {
        execute(
            """
            Line 1
            
            .whitespace width:{1cm} height:{3mm}
            
            Line 2
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Line 1</p><div class=\"whitespace\" style=\"width: 1.0cm; height: 3.0mm;\"></div><p>Line 2</p>",
                it,
            )
        }
    }

    @Test
    fun `case transformation`() {
        execute("Hello, World! .uppercase {Hello, World!} .lowercase {Hello, World!} .capitalize {hello, world!}") {
            assertEquals(
                "<p>Hello, World! HELLO, WORLD! hello, world! Hello, world!</p>",
                it,
            )
        }
    }

    @Test
    fun `quote type`() {
        arrayOf("> Tip: this is a tip", "> [!TIP]\n> this is a tip").forEach { source ->
            execute(".doclang {English}\n$source") {
                assertEquals(
                    "<blockquote class=\"tip\" style=\"--quote-type-label: 'Tip';\" data-labeled=\"\"><p>this is a tip</p></blockquote>",
                    it,
                )
            }
        }
    }

    @Test
    fun `quote type with non-paragraph first child`() {
        execute(
            """
            .doclang {English}
            > [!TIP]
            > - this is
            > - a tip
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote class=\"tip\" style=\"--quote-type-label: 'Tip';\" data-labeled=\"\">" +
                    "<p data-accept-empty=\"\"></p>" +
                    "<ul><li>this is</li><li>a tip</li></ul>" +
                    "</blockquote>",
                it,
            )
        }
    }

    @Test
    fun `quote type with empty content`() {
        execute(
            """
            .doclang {English}
            > [!TIP]
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote class=\"tip\" style=\"--quote-type-label: 'Tip';\" data-labeled=\"\"><p data-accept-empty=\"\"></p></blockquote>",
                it,
            )
        }
    }

    @Test
    fun `quote attribution`() {
        execute(
            """
            .doclang {Italian}
            > Tip: you could try Quarkdown.  
            > It's a cool language!
            > - **iamgio**
            
            > Important: leave a feedback!
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote class=\"tip\" style=\"--quote-type-label: 'Consiglio';\" data-labeled=\"\">" +
                    "<p>you could try Quarkdown.<br />" +
                    "It&rsquo;s a cool language!</p>" +
                    "<p class=\"attribution\"><strong>iamgio</strong></p>" +
                    "</blockquote>" +
                    "<blockquote class=\"important\" style=\"--quote-type-label: 'Importante';\" data-labeled=\"\">" +
                    "<p>leave a feedback!</p>" +
                    "</blockquote>",
                it,
            )
        }
    }

    @Test
    fun `text alignment`() {
        execute(
            """
            A
            
            .align {end}
                ### B
            C
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>A</p>" +
                    "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\"><h3>B</h3></div>" +
                    "<p>C</p>",
                it,
            )
        }
    }
}
