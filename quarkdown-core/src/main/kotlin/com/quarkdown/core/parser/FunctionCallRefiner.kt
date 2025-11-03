package com.quarkdown.core.parser

import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.UncheckedFunctionCall
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall

/**
 * Refines a [WalkedFunctionCall], which is a context-free syntactical element extracted from a function call in a source code,
 * into a processed, context-aware [FunctionCallNode].
 *
 * This refiner also handles function chaining, which is represented by a linked list in the [WalkedFunctionCall] structure,
 * but is a tree in the [FunctionCallNode] structure.
 *
 * Example:
 * 1. Source code: `.foo {x}::bar {y}`
 * 2. Walked function call: `.foo {x}` -> `.bar {y}`
 * 3. Refinement: `.bar {.foo {x}} {y}`
 *
 * @param context context of the function call
 * @param call walked function call to refine
 * @param isBlock whether the function call is a block
 * @param sourceText if available, the source code of the whole function call
 * @param sourceRange if available, the range of the function call in the source code
 * @param initialArguments initial arguments to add to the function call (used internally for chaining)
 */
class FunctionCallRefiner(
    private val context: Context,
    private val call: WalkedFunctionCall,
    private val isBlock: Boolean,
    private val sourceText: CharSequence?,
    private val sourceRange: IntRange?,
    private val initialArguments: List<FunctionCallArgument> = emptyList(),
) {
    /**
     * Extracts arguments from the walked function [call].
     */
    private fun extractArguments(): List<FunctionCallArgument> {
        val arguments = initialArguments.toMutableList()

        // Inline function arguments.
        arguments +=
            call.arguments
                .asSequence()
                .map { arg ->
                    val raw = arg.value.trim()
                    // Convert the raw argument to an expression.
                    val expression = ValueFactory.safeExpression(raw, context)
                    FunctionCallArgument(
                        expression,
                        name = arg.name,
                        isBody = false,
                    )
                }

        // Body function argument.
        // A body argument is always the last one, it goes on a new line and each line is indented.
        call.bodyArgument?.takeUnless { it.value.isBlank() }?.value?.let { body ->
            // A body argument is treated as plain text, thus nested function calls are not executed by default.
            // They are executed if the argument is used as Markdown content from the referenced function,
            // that runs recursive lexing & parsing on the arg content, triggering function calls.
            val value = DynamicValue(body)

            arguments += FunctionCallArgument(value, isBody = true)
        }

        return arguments
    }

    /**
     * Refines the walked function [call] into a [FunctionCallNode].
     */
    fun toNode(): FunctionCallNode {
        val node = FunctionCallNode(context, call.name, extractArguments(), isBlock, sourceText, sourceRange)

        // Chaining: if this function call (A) is chained with another one (B),
        // then the result node is B(A).
        call.next?.let { next ->
            val call: UncheckedFunctionCall<*> = context.resolveUnchecked(node) // A
            val initialArguments = listOf(FunctionCallArgument(call)) // A as an argument for B

            val refiner = FunctionCallRefiner(context, next, isBlock, sourceText, sourceRange, initialArguments) // B(A)
            return refiner.toNode()
        }

        return node
    }
}
