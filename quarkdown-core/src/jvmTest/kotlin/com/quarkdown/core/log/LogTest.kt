package com.quarkdown.core.log

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [Log] level configuration.
 */
class LogTest {
    @AfterTest
    fun tearDown() {
        Log.level = LogLevel.INFO
    }

    @Test
    fun `lazy debug message is not evaluated below debug level`() {
        Log.level = LogLevel.INFO
        var evaluated = false
        Log.debug {
            evaluated = true
            "message"
        }
        assertFalse(evaluated)
    }

    @Test
    fun `lazy debug message is evaluated at debug level`() {
        Log.level = LogLevel.DEBUG
        var evaluated = false
        Log.debug {
            evaluated = true
            "message"
        }
        assertTrue(evaluated)
    }

    @Test
    fun `none level suppresses evaluation entirely`() {
        Log.level = LogLevel.NONE
        var evaluated = false
        Log.debug {
            evaluated = true
            "message"
        }
        assertFalse(evaluated)
    }
}
