package com.quarkdown.core.function.value

/**
 * If a [Value] subclass is adaptable, it can be converted to another [Value]
 * in case the parameter of the function it is passed to expects a different type.
 *
 * For example, a [DictionaryValue] can be adapted to an [IterableValue].
 *
 * @param T type of the value to adapt to
 */
interface AdaptableValue<T : InputValue<*>> {
    /**
     * Adapts the value to another type.
     */
    fun adapt(): T
}
