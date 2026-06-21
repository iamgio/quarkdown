package com.quarkdown.core

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.binding.RegularArgumentsBinder
import com.quarkdown.core.function.error.InvalidArgumentCountException
import com.quarkdown.core.function.error.ParameterAlreadyBoundException
import com.quarkdown.core.function.error.UnnamedArgumentAfterNamedException
import com.quarkdown.core.function.error.UnresolvedParameterException
import com.quarkdown.core.function.value.StringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for [RegularArgumentsBinder]: the component that pairs each function call argument
 * (positional, named, or body) with its corresponding parameter.
 */
class RegularArgumentsBinderTest {
    private fun param(
        name: String,
        index: Int,
        isExplicitlyBody: Boolean = false,
    ) = FunctionParameter(name, StringValue::class, index, isExplicitlyBody = isExplicitlyBody)

    private fun bind(
        parameters: List<FunctionParameter<*>>,
        arguments: List<FunctionCallArgument>,
    ): Map<FunctionParameter<*>, FunctionCallArgument> {
        val function =
            SimpleFunction(
                name = "test",
                parameters = parameters,
            ) { _, _ -> StringValue("") }
        return RegularArgumentsBinder(FunctionCall(function, arguments)).createBindings(parameters)
    }

    private fun Map<FunctionParameter<*>, FunctionCallArgument>.unwrap(parameter: FunctionParameter<*>): Any? =
        this[parameter]?.value?.unwrappedValue

    @Test
    fun `positional binding follows parameter order`() {
        val a = param("a", 0)
        val b = param("b", 1)
        val bindings =
            bind(
                listOf(a, b),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("y")),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("y", bindings.unwrap(b))
    }

    @Test
    fun `named binding ignores argument order`() {
        val a = param("a", 0)
        val b = param("b", 1)
        val bindings =
            bind(
                listOf(a, b),
                listOf(
                    FunctionCallArgument(StringValue("y"), name = "b"),
                    FunctionCallArgument(StringValue("x"), name = "a"),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("y", bindings.unwrap(b))
    }

    @Test
    fun `positional then named binding`() {
        val a = param("a", 0)
        val b = param("b", 1)
        val c = param("c", 2)
        val bindings =
            bind(
                listOf(a, b, c),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("z"), name = "c"),
                    FunctionCallArgument(StringValue("y"), name = "b"),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("y", bindings.unwrap(b))
        assertEquals("z", bindings.unwrap(c))
    }

    @Test
    fun `unnamed argument after named one throws`() {
        val a = param("a", 0)
        val b = param("b", 1)
        assertFailsWith<UnnamedArgumentAfterNamedException> {
            bind(
                listOf(a, b),
                listOf(
                    FunctionCallArgument(StringValue("x"), name = "a"),
                    FunctionCallArgument(StringValue("y")),
                ),
            )
        }
    }

    @Test
    fun `unknown parameter name throws`() {
        val a = param("a", 0)
        assertFailsWith<UnresolvedParameterException> {
            bind(
                listOf(a),
                listOf(FunctionCallArgument(StringValue("x"), name = "z")),
            )
        }
    }

    @Test
    fun `too many positional arguments throws`() {
        val a = param("a", 0)
        assertFailsWith<InvalidArgumentCountException> {
            bind(
                listOf(a),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("y")),
                ),
            )
        }
    }

    @Test
    fun `parameter bound positionally and by name throws`() {
        val a = param("a", 0)
        val b = param("b", 1)
        assertFailsWith<ParameterAlreadyBoundException> {
            bind(
                listOf(a, b),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("y"), name = "a"),
                ),
            )
        }
    }

    @Test
    fun `body argument falls back to last parameter when none is marked as body`() {
        val a = param("a", 0)
        val last = param("last", 1)
        val bindings =
            bind(
                listOf(a, last),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("content"), isBody = true),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("content", bindings.unwrap(last))
    }

    @Test
    fun `body argument binds to explicit body parameter even when not last`() {
        val a = param("a", 0)
        val body = param("body", 1, isExplicitlyBody = true)
        val c = param("c", 2)
        val bindings =
            bind(
                listOf(a, body, c),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("z")),
                    FunctionCallArgument(StringValue("content"), isBody = true),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("z", bindings.unwrap(c))
        assertEquals("content", bindings.unwrap(body))
    }

    @Test
    fun `explicit body parameter is excluded from positional binding`() {
        // The body parameter sits at position 0 but is reserved for the body argument,
        // so positional arguments must skip over it and bind to the remaining parameters.
        val body = param("body", 0, isExplicitlyBody = true)
        val a = param("a", 1)
        val b = param("b", 2)
        val bindings =
            bind(
                listOf(body, a, b),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("y")),
                    FunctionCallArgument(StringValue("content"), isBody = true),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("y", bindings.unwrap(b))
        assertEquals("content", bindings.unwrap(body))
    }

    @Test
    fun `explicit body parameter is excluded from named binding`() {
        val a = param("a", 0)
        val body = param("body", 1, isExplicitlyBody = true)
        assertFailsWith<UnresolvedParameterException> {
            bind(
                listOf(a, body),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("override"), name = "body"),
                    FunctionCallArgument(StringValue("content"), isBody = true),
                ),
            )
        }
    }

    @Test
    fun `explicit body parameter binds normally when no body argument is provided`() {
        // Without a body argument, isExplicitlyBody has no effect: the parameter is bindable
        // by name or position like any other.
        val a = param("a", 0)
        val body = param("body", 1, isExplicitlyBody = true)
        val bindings =
            bind(
                listOf(a, body),
                listOf(
                    FunctionCallArgument(StringValue("x")),
                    FunctionCallArgument(StringValue("y"), name = "body"),
                ),
            )
        assertEquals("x", bindings.unwrap(a))
        assertEquals("y", bindings.unwrap(body))
    }

    @Test
    fun `empty arguments and parameters produce empty bindings`() {
        assertEquals(0, bind(emptyList(), emptyList()).size)
    }
}
