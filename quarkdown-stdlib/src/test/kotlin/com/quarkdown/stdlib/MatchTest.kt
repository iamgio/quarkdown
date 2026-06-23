package com.quarkdown.stdlib

import com.quarkdown.core.assertNodeEquals
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.data.LambdaParameter
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Tests for the `.match` function.
 */
class MatchTest {
    private val context = MutableContext(QuarkdownFlavor)

    @BeforeTest
    fun setup() {
        context.attachMockPipeline()
    }

    /**
     * @return a lambda that wraps each matched substring in bold inline Markdown,
     *         producing a `Strong` node around the match
     */
    private fun strongLambda(): Lambda =
        Lambda(context, explicitParameters = listOf(LambdaParameter("m"))) { args, _ ->
            val matched = args.first().unwrappedValue.toString()
            DynamicValue("**$matched**")
        }

    @Test
    fun `empty content`() {
        val content = InlineMarkdownContent(emptyList())
        val result = match(content, "test", strongLambda()).unwrappedValue
        assertNodeEquals(content, result)
    }

    @Test
    fun `empty pattern`() {
        val content = InlineMarkdownContent(buildInline { text("Hello") })
        val result = match(content, "", strongLambda()).unwrappedValue
        assertNodeEquals(content, result)
    }

    @Test
    fun `no match`() {
        val content = InlineMarkdownContent(buildInline { text("Hello") })
        val result = match(content, "xyz", strongLambda()).unwrappedValue
        assertNodeEquals(content, result)
    }

    @Test
    fun `single match`() {
        val content = InlineMarkdownContent(buildInline { text("Hello") })
        val expected =
            InlineMarkdownContent(
                buildInline {
                    text("He")
                    strong { text("llo") }
                },
            )
        val result = match(content, "llo", strongLambda()).unwrappedValue
        assertNodeEquals(expected, result)
    }

    @Test
    fun `multiple matches`() {
        val content = InlineMarkdownContent(buildInline { text("Hey hello hey hello") })
        val expected =
            InlineMarkdownContent(
                buildInline {
                    text("Hey ")
                    strong { text("hello") }
                    text(" hey ")
                    strong { text("hello") }
                },
            )
        val result = match(content, "hello", strongLambda()).unwrappedValue
        assertNodeEquals(expected, result)
    }

    @Test
    fun `regex match`() {
        val content = InlineMarkdownContent(buildInline { text("abc123def") })
        val expected =
            InlineMarkdownContent(
                buildInline {
                    text("abc")
                    strong { text("123") }
                    text("def")
                },
            )
        val result = match(content, "\\d+", strongLambda()).unwrappedValue
        assertNodeEquals(expected, result)
    }

    @Test
    fun `rich content preserved`() {
        val content =
            InlineMarkdownContent(
                buildInline {
                    text("Hello ")
                    emphasis { text("world") }
                    text(" and ")
                    +TextTransform(
                        TextTransformData(variant = TextTransformData.Variant.SMALL_CAPS),
                        text = buildInline { text("world") },
                    )
                },
            )
        val expected =
            InlineMarkdownContent(
                buildInline {
                    text("Hello ")
                    emphasis {
                        text("wo")
                        strong { text("rl") }
                        text("d")
                    }
                    text(" and ")
                    +TextTransform(
                        TextTransformData(variant = TextTransformData.Variant.SMALL_CAPS),
                        text =
                            buildInline {
                                text("wo")
                                strong { text("rl") }
                                text("d")
                            },
                    )
                },
            )
        val result = match(content, "rl", strongLambda()).unwrappedValue
        assertNodeEquals(expected, result)
    }

    @Test
    fun `lambda controls replacement node`() {
        val content = InlineMarkdownContent(buildInline { text("Hello world") })
        val emphasisLambda =
            Lambda(context, explicitParameters = listOf(LambdaParameter("m"))) { args, _ ->
                val matched = args.first().unwrappedValue.toString()
                DynamicValue("_${matched.uppercase()}_")
            }
        val expected =
            InlineMarkdownContent(
                buildInline {
                    text("Hello ")
                    emphasis { text("WORLD") }
                },
            )
        val result = match(content, "world", emphasisLambda).unwrappedValue
        assertNodeEquals(expected, result)
    }
}
