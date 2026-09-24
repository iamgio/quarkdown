package com.quarkdown.core

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.function.dsl.functionCallArguments
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.InlineMarkdownContentValue
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.data.EvaluableString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Tests for the [functionCallArguments] DSL used by primitive-backed nodes.
 * @see com.quarkdown.core.function.dsl.FunctionCallArgumentsBuilder
 */
class FunctionCallArgumentsBuilderTest {
    @Test
    fun `empty builder yields empty list`() {
        assertEquals(emptyList(), functionCallArguments { })
    }

    @Test
    fun `arg adds named argument`() {
        val args = functionCallArguments { arg("depth", number(3)) }
        assertEquals(1, args.size)
        assertEquals("depth", args[0].name)
        assertEquals(NumberValue(3), args[0].expression)
    }

    @Test
    fun `multiple args preserve order`() {
        val args =
            functionCallArguments {
                arg("a", string("x"))
                arg("b", boolean(true))
                arg("c", number(1))
            }
        assertEquals(listOf("a", "b", "c"), args.map { it.name })
    }

    @Test
    fun `inline wraps InlineContent as inline markdown value`() {
        val content: InlineContent = listOf(Text("hi"))
        val expression = functionCallArguments { arg("x", inline(content)) }[0].expression
        val value = assertIs<InlineMarkdownContentValue>(expression)
        assertEquals(content, value.unwrappedValue.children)
    }

    @Test
    fun `inline maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", inline(null)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `block wraps single node in MarkdownContent`() {
        val node = Text("hi")
        val expression = functionCallArguments { arg("x", block(node)) }[0].expression
        val value = assertIs<MarkdownContentValue>(expression)
        assertEquals(listOf(node), value.unwrappedValue.children)
    }

    @Test
    fun `block wraps list of nodes in MarkdownContent`() {
        val nodes = listOf(Text("a"), Text("b"))
        val expression = functionCallArguments { arg("x", block(nodes)) }[0].expression
        val value = assertIs<MarkdownContentValue>(expression)
        assertEquals(nodes, value.unwrappedValue.children)
    }

    @Test
    fun `block maps null single node to NoneValue`() {
        val expression = functionCallArguments { arg("x", block(null as Node?)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `block maps null list to NoneValue`() {
        val expression = functionCallArguments { arg("x", block(null as List<Node>?)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `string wraps non-null value`() {
        val expression = functionCallArguments { arg("x", string("hello")) }[0].expression
        assertEquals(StringValue("hello"), expression)
    }

    @Test
    fun `string maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", string(null)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `number wraps as NumberValue`() {
        val expression = functionCallArguments { arg("x", number(42)) }[0].expression
        assertEquals(NumberValue(42), expression)
    }

    @Test
    fun `number maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", number(null)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `boolean wraps as BooleanValue`() {
        val expression = functionCallArguments { arg("x", boolean(true)) }[0].expression
        assertEquals(BooleanValue(true), expression)
    }

    @Test
    fun `boolean maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", boolean(null)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `evaluable wraps as ObjectValue holding EvaluableString`() {
        val expression = functionCallArguments { arg("x", evaluable("a + b")) }[0].expression
        val value = assertIs<ObjectValue<*>>(expression)
        assertEquals(EvaluableString("a + b"), value.unwrappedValue)
    }

    @Test
    fun `evaluable maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", evaluable(null)) }[0].expression
        assertSame(NoneValue, expression)
    }

    @Test
    fun `obj wraps non-null as ObjectValue`() {
        val payload = 123
        val expression = functionCallArguments { arg("x", obj(payload)) }[0].expression
        assertEquals(ObjectValue(payload), expression)
    }

    @Test
    fun `obj maps null to NoneValue`() {
        val expression = functionCallArguments { arg("x", obj(null)) }[0].expression
        assertSame(NoneValue, expression)
    }
}
