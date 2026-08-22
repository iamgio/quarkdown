package com.quarkdown.processor.coercion

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [CoercionPlanner]: the build-time decision of how a parameter type is filled.
 */
class CoercionPlannerTest {
    private val candidates =
        listOf(
            FactoryCandidate("string", "kotlin.String", requiresContext = false),
            FactoryCandidate("number", "kotlin.Number", requiresContext = false),
            FactoryCandidate("size", "com.quarkdown.core.document.size.Size", requiresContext = false),
            FactoryCandidate("iterable", "kotlin.collections.Iterable", requiresContext = true),
            FactoryCandidate("blockMarkdown", "com.quarkdown.core.ast.MarkdownContent", requiresContext = true),
        )

    private fun shape(
        qualifiedName: String,
        simpleName: String,
        supertypes: Set<String> = emptySet(),
        isEnum: Boolean = false,
        isNullable: Boolean = false,
    ) = TypeShape(qualifiedName, simpleName, supertypes, isEnum, isNullable)

    private fun plan(
        type: TypeShape,
        isInjected: Boolean = false,
    ) = CoercionPlanner.plan(type, isInjected, candidates)

    @Test
    fun `exact type match selects its factory`() {
        val result = assertIs<CoercionPlan.ViaFactory>(plan(shape("kotlin.String", "String")))
        assertEquals("com.quarkdown.core.function.ParameterType.Static(\"String\")", result.parameterTypeExpression)
        assertEquals("com.quarkdown.core.function.value.factory.ValueFactory.string(it)", result.factoryExpression)
    }

    @Test
    fun `a subtype resolves through its supertypes`() {
        val result = assertIs<CoercionPlan.ViaFactory>(plan(shape("kotlin.Int", "Int", setOf("kotlin.Number"))))
        assertContains(result.factoryExpression, "ValueFactory.number(it)")
    }

    @Test
    fun `a context-requiring factory receives the call context`() {
        val type = shape("kotlin.collections.List", "List", setOf("kotlin.collections.Iterable"))
        val result = assertIs<CoercionPlan.ViaFactory>(plan(type))
        assertContains(result.factoryExpression, "ValueFactory.iterable(it, call.requireContext())")
    }

    @Test
    fun `an enum bakes in its entries`() {
        val type = shape("com.quarkdown.core.ast.attributes.style.NodeStyle.Alignment", "Alignment", isEnum = true)
        val result = assertIs<CoercionPlan.ViaFactory>(plan(type))
        assertContains(result.factoryExpression, "ValueFactory.enum(it,")
        assertContains(
            result.factoryExpression,
            "com.quarkdown.core.ast.attributes.style.NodeStyle.Alignment.entries.toTypedArray()",
        )
        assertEquals("com.quarkdown.core.function.ParameterType.Static(\"Alignment\")", result.parameterTypeExpression)
    }

    @Test
    fun `a dynamic parameter passes through`() {
        val type = shape("com.quarkdown.core.function.value.DynamicValue", "DynamicValue")
        val result = assertIs<CoercionPlan.ViaFactory>(plan(type))
        assertEquals("com.quarkdown.core.function.ParameterType.Dynamic", result.parameterTypeExpression)
    }

    @Test
    fun `a lambda parameter is marked as a lambda block`() {
        val type = shape("com.quarkdown.core.function.value.data.Lambda", "Lambda")
        val result = assertIs<CoercionPlan.ViaFactory>(plan(type))
        assertEquals("com.quarkdown.core.function.ParameterType.LambdaBlock", result.parameterTypeExpression)
        assertContains(result.factoryExpression, "ValueFactory.lambda(it, call.requireContext())")
    }

    @Test
    fun `a parameter already typed as a value passes through`() {
        val type =
            shape(
                "com.quarkdown.core.function.value.OutputValue",
                "OutputValue",
                setOf("com.quarkdown.core.function.value.Value"),
            )
        val result = assertIs<CoercionPlan.ViaFactory>(plan(type))
        assertEquals("com.quarkdown.core.function.ParameterType.Static(\"OutputValue\")", result.parameterTypeExpression)
        assertContains(result.factoryExpression, "as? com.quarkdown.core.function.value.Value<*>")
    }

    @Test
    fun `an injected context becomes a cast local`() {
        val type = shape("com.quarkdown.core.context.MutableContext", "MutableContext")
        val result = assertIs<CoercionPlan.ViaInjection>(plan(type, isInjected = true))
        assertContains(result.parameterTypeExpression, "InjectionKind.CONTEXT")
        assertEquals(
            "call.requireContext() as com.quarkdown.core.context.MutableContext",
            result.localExpression,
        )
    }

    @Test
    fun `an injected function call is passed straight through`() {
        val type = shape("com.quarkdown.core.function.call.FunctionCall", "FunctionCall")
        val result = assertIs<CoercionPlan.ViaInjection>(plan(type, isInjected = true))
        assertEquals("call", result.localExpression)
    }

    @Test
    fun `an injected source node is read off the call`() {
        val type = shape("com.quarkdown.core.ast.quarkdown.FunctionCallNode", "FunctionCallNode")
        val result = assertIs<CoercionPlan.ViaInjection>(plan(type, isInjected = true))
        assertEquals("call.sourceNode", result.localExpression)
    }

    @Test
    fun `an injected non-injectable type is unsupported`() {
        val result = assertIs<CoercionPlan.Unsupported>(plan(shape("kotlin.String", "String"), isInjected = true))
        assertContains(result.reason, "not injectable")
    }

    @Test
    fun `an unmappable type is unsupported`() {
        val result = assertIs<CoercionPlan.Unsupported>(plan(shape("java.io.File", "File")))
        assertContains(result.reason, "no ValueFactory")
    }

    @Test
    fun `ambiguous candidates are unsupported`() {
        val ambiguous =
            listOf(
                FactoryCandidate("a", "com.example.Left", requiresContext = false),
                FactoryCandidate("b", "com.example.Right", requiresContext = false),
            )
        val type = shape("com.example.Both", "Both", setOf("com.example.Left", "com.example.Right"))
        val result = assertIs<CoercionPlan.Unsupported>(CoercionPlanner.plan(type, false, ambiguous))
        assertContains(result.reason, "ambiguous")
    }
}
