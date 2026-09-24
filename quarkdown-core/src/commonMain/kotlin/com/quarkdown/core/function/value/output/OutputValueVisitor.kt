package com.quarkdown.core.function.value.output

import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.PairValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.UnorderedCollectionValue
import com.quarkdown.core.function.value.VoidValue

/**
 * A visitor that produces values the same type for each [com.quarkdown.core.function.value.OutputValue] type.
 */
interface OutputValueVisitor<T> {
    fun visit(value: StringValue): T

    fun visit(value: NumberValue): T

    fun visit(value: BooleanValue): T

    fun visit(value: ObjectValue<*>): T

    fun visit(value: OrderedCollectionValue<*>): T

    fun visit(value: UnorderedCollectionValue<*>): T

    fun visit(value: GeneralCollectionValue<*>): T

    fun visit(value: PairValue<*, *>): T

    fun visit(value: DictionaryValue<*>): T

    fun visit(value: NodeValue): T

    fun visit(value: NoneValue): T

    fun visit(value: VoidValue): T

    fun visit(value: DynamicValue): T
}
