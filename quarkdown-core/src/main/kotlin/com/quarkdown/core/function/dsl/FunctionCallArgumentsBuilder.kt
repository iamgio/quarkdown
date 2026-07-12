package com.quarkdown.core.function.dsl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.data.EvaluableString
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * DSL marker for [FunctionCallArgumentsBuilder].
 */
@DslMarker
private annotation class FunctionCallArgumentsDsl

/**
 * Builder for the argument list of a synthesized function call, typically used to
 * implement [PrimitiveFunctionBackedNode.toFunctionCallArguments].
 *
 * Provides concise helpers that wrap common value shapes into [Expression]s,
 * mapping `null` to [NoneValue] where an argument is optional.
 *
 * @see functionCallArguments
 */
@FunctionCallArgumentsDsl
class FunctionCallArgumentsBuilder internal constructor() {
    private val arguments = mutableListOf<FunctionCallArgument>()

    /**
     * Appends a named argument.
     * @param name argument name, matching the corresponding parameter of the backing function
     * @param expression argument expression, typically produced by one of the helpers in this scope
     */
    fun arg(
        name: String,
        expression: Expression,
    ) {
        arguments += FunctionCallArgument(name = name, expression = expression)
    }

    /**
     * @return [content] wrapped as an inline Markdown expression, or [NoneValue] if `null`
     */
    fun inline(content: InlineContent?): Expression = content?.let { InlineMarkdownContent(it).wrappedAsValue() } ?: NoneValue

    /**
     * @return a block Markdown expression containing [node] as its single child, or [NoneValue] if `null`
     */
    fun block(node: Node?): Expression = node?.let { MarkdownContent(listOf(it)).wrappedAsValue() } ?: NoneValue

    /**
     * @return a block Markdown expression wrapping [nodes], or [NoneValue] if `null`
     */
    fun block(nodes: List<Node>?): Expression = nodes?.let { MarkdownContent(it).wrappedAsValue() } ?: NoneValue

    /**
     * @return [value] wrapped as a string expression, or [NoneValue] if `null`
     */
    fun string(value: String?): Expression = value?.let(::StringValue) ?: NoneValue

    /**
     * @return [value] wrapped as a numeric expression, or [NoneValue] if `null`
     */
    fun number(value: Number?): Expression = value?.let(::NumberValue) ?: NoneValue

    /**
     * @return [value] wrapped as a boolean expression, or [NoneValue] if `null`
     */
    fun boolean(value: Boolean?): Expression = value?.let(::BooleanValue) ?: NoneValue

    /**
     * @return [value] wrapped as an evaluable string, expanded (including nested function calls)
     *         when the resulting argument is evaluated, or [NoneValue] if `null`
     */
    fun evaluable(value: String?): Expression = value?.let { ObjectValue(EvaluableString(it)) } ?: NoneValue

    /**
     * @return an arbitrary [value] wrapped as an object expression, or [NoneValue] if `null`
     */
    fun obj(value: Any?): Expression = value?.let { ObjectValue(it) } ?: NoneValue

    internal fun build(): List<FunctionCallArgument> = arguments.toList()
}

/**
 * Builds a list of [FunctionCallArgument]s via the [FunctionCallArgumentsBuilder] DSL.
 * Preferred over listing arguments by hand in [PrimitiveFunctionBackedNode.toFunctionCallArguments].
 *
 * ```
 * override fun toFunctionCallArguments() =
 *     functionCallArguments {
 *         arg("content", inline(label))
 *         arg("url", string(url))
 *         arg("title", inline(title))
 *     }
 * ```
 */
fun functionCallArguments(block: FunctionCallArgumentsBuilder.() -> Unit): List<FunctionCallArgument> =
    FunctionCallArgumentsBuilder().apply(block).build()
