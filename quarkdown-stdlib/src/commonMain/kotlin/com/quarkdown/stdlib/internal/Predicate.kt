package com.quarkdown.stdlib.internal

import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.data.Lambda
import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * Invokes a lambda as a predicate.
 * @param arguments arguments of the lambda action
 * @return whether the lambda evaluates to `true` for the given arguments
 * @throws IllegalArgumentException if the lambda does not produce a boolean
 */
internal fun Lambda.test(arguments: List<Value<*>>): Boolean =
    invoke<Boolean, BooleanValue>(arguments) { ValueFactory.boolean(it) }.unwrappedValue

/**
 * @see test
 */
internal fun Lambda.test(vararg arguments: Value<*>): Boolean = test(arguments.toList())
