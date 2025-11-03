package com.quarkdown.test

import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.core.function.error.ParameterAlreadyBoundException
import com.quarkdown.core.function.error.UnresolvedReferenceException
import com.quarkdown.test.util.execute
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Tests for strict error handling, where errors throw exceptions.
 */
class StrictErrorHandlingTest {
    @Test
    fun `error on unresolved reference`() {
        assertFailsWith<UnresolvedReferenceException> {
            execute(".nonexistent") {}
        }
    }

    @Test
    fun `error on argument count`() {
        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2}") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2} {5} {9}") {}
        }
    }

    @Test
    fun `error on positional parameter already bound`() {
        assertFailsWith<ParameterAlreadyBoundException> {
            execute(".sum {2} a:{3}") {}
        }
    }

    @Test
    fun `error on named parameter already bound`() {
        assertFailsWith<ParameterAlreadyBoundException> {
            execute(".sum a:{2} a:{3}") {}
        }
    }

    @Test
    fun `error on argument type`() {
        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {a} {3}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {2} {.multiply {3} {a}}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".if {hello}\n\t.sum {2} {3} {1}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".row alignment:{center} cross:{hello}\n\tHi") {}
        }
    }

    @Test
    fun `error on document type`() {
        assertFailsWith<InvalidFunctionCallException> {
            execute(".doctype {plain}\n.slides") {}
        }
    }

    @Test
    fun `runtime error`() {
        assertFailsWith<FunctionCallRuntimeException> {
            execute(".csv {nonexistent}") {}
        }.also { exception ->
            assertIs<IllegalArgumentException>(exception.cause)
        }
    }
}
