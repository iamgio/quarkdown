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
                    "&ndash; all rights reserved.\n" +
                    "&rArr; Quarkdown &ne; other Markdown flavors&hellip; &larr;" +
                    "</p></blockquote>",
                it,
            )
        }
    }

    @Test
    fun `text in headings`() {
        execute(".noautopagebreak\n# Title\n Hello, world!\n## Subtitle\nHello, world!") {
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
                "<p>This is a <span class=\"size-tiny\" style=\"font-variant: small-caps;\">small text</span></p>",
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
            assertEquals("<p>Line 1</p><span>&nbsp;</span><p>Line 2 after a long break</p>", it)
        }
    }

    @Test
    fun `sized whitespace`() {
        execute("A .whitespace width:{1cm} B") {
            assertEquals("<p>A <div style=\"width: 1.0cm;\"></div> B</p>", it)
        }

        execute("A .whitespace width:{1cm} height:{3mm} B") {
            assertEquals("<p>A <div style=\"width: 1.0cm; height: 3.0mm;\"></div> B</p>", it)
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
