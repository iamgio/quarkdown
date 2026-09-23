package com.quarkdown.core.function.value.output

import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.function.value.OutputValue

/**
 * Factory that produces [OutputValueVisitor]s that map function output values ([OutputValue]) into other objects.
 * @param T type of the output of the visit operations
 */
interface OutputValueVisitorFactory<T> {
    /**
     * @return a mapper that produces results for block function calls ([FunctionCallNode.isBlock] is true)
     */
    fun block(): OutputValueVisitor<T>

    /**
     * @return a mapper that produces results for inline function calls ([FunctionCallNode.isBlock] is false)
     */
    fun inline(): OutputValueVisitor<T>
}
