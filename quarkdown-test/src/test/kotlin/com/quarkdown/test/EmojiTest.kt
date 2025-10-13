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
            assertEquals("</p>\uD83D\uDC4B\uD83C\uDFFE</p>", it)
        }
    }

    @Test
    fun `emoji with two skin tones`() {
        execute(".emoji {people-holding-hands~medium-light,medium-dark}") {
            assertEquals("<p>\uD83C\uDFFC\u200D\uD83E\uDD1D</p>", it)
        }
    }

    @Test
    fun `unknown emoji`() {
        execute(".emoji {unknown}") {
            assertEquals("<p>:unknown:</p>", it)
        }
    }
}
