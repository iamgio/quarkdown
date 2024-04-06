package eu.iamgio.quarkdown.function.value.output

import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue

/**
 * A visitor that produces values the same type for each [eu.iamgio.quarkdown.function.value.OutputValue] type.
 */
interface OutputValueVisitor<T> {
    fun visit(value: StringValue): T

    fun visit(value: NumberValue): T

    fun visit(value: NodeValue): T

    fun visit(value: VoidValue): T
}
