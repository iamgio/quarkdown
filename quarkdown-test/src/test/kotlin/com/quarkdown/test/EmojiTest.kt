package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the `emoji` stdlib function.
 */
class EmojiTest {
    @Test
    fun `normal emoji`() {
        execute(".emoji {wink}") {
            assertEquals("<p>\uD83D\uDE09</p>", it)
        }
    }

    @Test
    fun `emoji with skin tone`() {
        execute(".emoji {waving-hand~medium-dark}") {
            assertEquals("<p>\uD83D\uDC4B\uD83C\uDFFE</p>", it)
        }
    }

    @Test
    fun `emoji with two skin tones`() {
        execute(".emoji {people-holding-hands~medium-light,medium-dark}") {
            assertEquals("<p>\uD83E\uDDD1\uD83C\uDFFC&zwj;\uD83E\uDD1D&zwj;\uD83E\uDDD1\uD83C\uDFFE</p>", it)
        }
    }

    @Test
    fun `unknown emoji`() {
        execute(".emoji {unknown}") {
            assertEquals("<p>:unknown:</p>", it)
        }
    }

    @Test
    fun `all emojis enumerated into a table`() {
        execute(
            """
            .var {headers}
                - Emoji
                - Code

            .tablebyrows {.headers}
                .foreach {.allemojis}
                    emoji code:
                    .pair {.emoji} {.code::codespan}
            """.trimIndent(),
        ) {
            val prefix =
                "<table><thead><tr><th>Emoji</th><th>Code</th></tr></thead><tbody>" +
                    "<tr><td>\uD83D\uDE00</td><td><span class=\"codespan-content\"><code>smile</code></span></td></tr>" +
                    "<tr><td>\uD83D\uDE03</td><td><span class=\"codespan-content\"><code>smile-with-big-eyes</code></span></td></tr>" +
                    "<tr><td>\uD83D\uDE04</td><td><span class=\"codespan-content\"><code>grin</code></span></td></tr><tr>"
            assertEquals(
                prefix,
                it.toString().substring(0, prefix.length),
            )
        }
    }
}
