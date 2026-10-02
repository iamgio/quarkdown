package com.quarkdown.core.function.library

import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.value.StringValue
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for [Library].
 */
class LibraryTest {
    private fun function(
        name: String,
        output: String = name,
    ) = SimpleFunction(name, parameters = emptyList()) { _, _ -> StringValue(output) }

    @Test
    fun `finds functions by name`() {
        val foo = function("foo")
        val bar = function("bar")
        val library = Library("lib", setOf(foo, bar))
        assertSame(foo, library.findFunction("foo"))
        assertSame(bar, library.findFunction("bar"))
    }

    @Test
    fun `missing function is null`() {
        val library = Library("lib", setOf(function("foo")))
        assertNull(library.findFunction("bar"))
        assertNull(Library("empty", emptySet()).findFunction("foo"))
    }

    @Test
    fun `first declared function wins on duplicate names`() {
        val first = function("foo", output = "first")
        val second = function("foo", output = "second")
        val library = Library("lib", linkedSetOf(first, second))
        assertSame(first, library.findFunction("foo"))
    }

    @Test
    fun `copies index their own functions`() {
        val foo = function("foo")
        val library = Library("lib", setOf(foo))
        library.findFunction("foo")
        val copy = library.copy(functions = setOf(function("bar")))
        assertNull(copy.findFunction("foo"))
    }
}
