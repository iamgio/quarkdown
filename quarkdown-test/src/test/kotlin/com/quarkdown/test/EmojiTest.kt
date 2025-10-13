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
            assertEquals("\uD83D\uDE09", it)
        }
    }

    @Test
    fun `emoji with skin tone`() {
        execute(".emoji {waving-hand~medium-dark}") {
            assertEquals("\uD83D\uDC4B\uD83C\uDFFF", it)
        }
    }

    @Test
    fun `emoji with two skin tones`() {
        execute(".emoji {people-holding-hands~medium-light,medium-dark}") {
            assertEquals("\uD83E\uDDD1\uD83C\uDFFE\u200D\uD83E\uDDD1\uD83C\uDFFF", it)
        }
    }

    @Test
    fun `unknown emoji`() {
        execute(".emoji {unknown}") {
            assertEquals(":unknown:", it)
        }
    }
}
