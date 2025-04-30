package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for function call chaining.
 */
class FunctionCallChainingTest {
    @Test
    fun math() {
        execute(".sum {2} {1}::subtract {1}") {
            assertEquals("<p>2</p>", it)
        }

        execute(".sum {2} {1}::subtract {4}::multiply {2}") {
            assertEquals("<p>-2</p>", it)
        }
    }

    @Test
    fun functional() {
        execute(".sum {2} {4}::subtract {1}::takeif {@lambda x: .iseven {.x}}::otherwise {Odd number!}") {
            assertEquals("<p>Odd number!</p>", it)
        }

        execute(".sum {2} {4}::subtract {1}::takeif {@lambda x: .iseven {.x}::not}::otherwise {Odd number!}") {
            assertEquals("<p>5</p>", it)
        }
    }
}
