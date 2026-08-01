package com.quarkdown.core

import com.quarkdown.core.function.Function
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.signatureAsString
import com.quarkdown.core.function.value.VoidValue
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [signatureAsString], the human-readable renderer used by error messages.
 */
class FunctionSignatureAsStringTest {
    private fun function(
        name: String,
        vararg parameters: FunctionParameter<*>,
    ): Function<VoidValue> =
        SimpleFunction(
            name = name,
            parameters = parameters.toList(),
        ) { _, _ -> VoidValue }

    private fun param(
        name: String,
        type: KClass<*>,
        isOptional: Boolean = false,
        isInjected: Boolean = false,
    ): FunctionParameter<*> =
        FunctionParameter(
            name = name,
            type = type,
            index = 0,
            isOptional = isOptional,
            isInjected = isInjected,
        )

    @Test
    fun `no parameters includes the function name and empty parens`() {
        assertEquals("foo()", function("foo").signatureAsString())
    }

    @Test
    fun `no parameters and includeName=false renders just parens`() {
        assertEquals("()", function("foo").signatureAsString(includeName = false))
    }

    @Test
    fun `required parameter renders as 'Type name'`() {
        val function = function("greet", param("name", String::class))
        assertEquals("greet(String name)", function.signatureAsString())
    }

    @Test
    fun `multiple parameters are comma-separated`() {
        val function =
            function(
                "add",
                param("x", Int::class),
                param("y", Int::class),
            )
        assertEquals("add(Int x, Int y)", function.signatureAsString())
    }

    @Test
    fun `optional parameter is prefixed with 'optional '`() {
        val function =
            function(
                "greet",
                param("name", String::class),
                param("greeting", String::class, isOptional = true),
            )
        assertEquals("greet(String name, optional String greeting)", function.signatureAsString())
    }

    @Test
    fun `includeName=false suppresses only the name, not the parameter list`() {
        val function = function("greet", param("name", String::class))
        assertEquals("(String name)", function.signatureAsString(includeName = false))
    }

    @Test
    fun `injected parameters are omitted from the signature`() {
        val function =
            function(
                "greet",
                param("context", String::class, isInjected = true),
                param("name", String::class),
            )
        assertEquals("greet(String name)", function.signatureAsString())
    }

    @Test
    fun `only-injected parameter list renders as empty parens`() {
        val function = function("greet", param("context", String::class, isInjected = true))
        assertEquals("greet()", function.signatureAsString())
    }
}
