package com.quarkdown.core

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.ScopeContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.core.function.value.wrappedAsValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for [Lambda] invocation.
 */
class LambdaTest {
    private val context = MutableContext(QuarkdownFlavor)

    @Test
    fun `no parameters, no return`() {
        val lambda =
            Lambda(context) { args, ctx ->
                assertNotEquals(context, ctx)
                assertIs<ScopeContext>(ctx)
                VoidValue
            }
        lambda.invoke<Unit, VoidValue>()
    }

    @Test
    fun `no parameters, with return`() {
        val lambda =
            Lambda(context) { args, _ ->
                StringValue("Hello")
            }
        assertEquals(
            "Hello",
            lambda.invoke<String, StringValue>().unwrappedValue,
        )
    }

    @Test
    fun `one explicit parameter`() {
        val lambda =
            Lambda(
                context,
                listOf(LambdaParameter("myparam")),
            ) { args, ctx ->
                assertEquals(1, args.size)
                assertNotNull(ctx.getFunctionByName("myparam"))
                VoidValue
            }
        assertNull(context.getFunctionByName("myparam"))
        lambda.invoke<Unit, VoidValue>("Hello".wrappedAsValue())
    }

    @Test
    fun `one implicit parameter`() {
        val lambda =
            Lambda(context) { args, ctx ->
                assertEquals(1, args.size)
                assertNotNull(ctx.getFunctionByName("1"))
                VoidValue
            }
        assertNull(context.getFunctionByName("1"))
        lambda.invoke<Unit, VoidValue>("Hello".wrappedAsValue())
    }

    @Test
    fun `optional parameter`() {
        val lambda =
            Lambda(
                context,
                listOf(LambdaParameter("myparam", true)),
            ) { args, ctx ->
                StringValue((args.singleOrNull() as? StringValue)?.unwrappedValue ?: "none")
            }
        assertEquals(
            "Hello",
            lambda.invoke<String, StringValue>("Hello".wrappedAsValue()).unwrappedValue,
        )
        assertEquals(
            "none",
            lambda.invoke<String, StringValue>().unwrappedValue,
        )
    }

    @Test
    fun `two parameters, one optional`() {
        val lambda =
            Lambda(
                context,
                listOf(
                    LambdaParameter("myparam1"),
                    LambdaParameter("myparam2", true),
                ),
            ) { args, ctx ->
                StringValue(
                    (0..1).joinToString {
                        (args[it] as? StringValue)?.unwrappedValue ?: "none"
                    },
                )
            }
        assertEquals(
            "Hello, world",
            lambda
                .invoke<String, StringValue>(
                    "Hello".wrappedAsValue(),
                    "world".wrappedAsValue(),
                ).unwrappedValue,
        )
        assertEquals(
            "Hello, none",
            lambda.invoke<String, StringValue>("Hello".wrappedAsValue()).unwrappedValue,
        )
        assertFails {
            lambda.invoke<String, StringValue>()
        }
        assertFails {
            lambda.invoke<String, StringValue>("Hello".wrappedAsValue(), "world".wrappedAsValue(), "extra".wrappedAsValue())
        }
    }

    /**
     * @see com.quarkdown.core.function.value.AdaptableValue
     */
    @Test
    fun `adapted result`() {
        val lambda =
            Lambda(context) { args, ctx ->
                NodeValue(Text("Hello"))
            }
        assertIs<Text>(
            lambda
                .invoke<MarkdownContent, MarkdownContentValue>()
                .unwrappedValue.children
                .single(),
        )
    }
}
