package eu.iamgio.quarkdown.function.value.output

import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.UnorderedCollectionValue
import eu.iamgio.quarkdown.function.value.VoidValue

/**
 * A visitor that produces values the same type for each [eu.iamgio.quarkdown.function.value.OutputValue] type.
 */
interface OutputValueVisitor<T> {
    fun visit(value: StringValue): T

    fun visit(value: NumberValue): T

    fun visit(value: BooleanValue): T

    fun visit(value: ObjectValue<*>): T

    fun visit(value: OrderedCollectionValue<*>): T

    fun visit(value: UnorderedCollectionValue<*>): T

    fun visit(value: GeneralCollectionValue<*>): T

    fun visit(value: NodeValue): T

    fun visit(value: VoidValue): T

    fun visit(value: DynamicValue): T
}
