package eu.iamgio.quarkdown.function.value

/**
 *
 */
data class Value<T, VT : ValueType<T>>(
    val value: T,
    private val type: VT,
) {
    fun isOf(type: ValueType<*>) = this.type == type
}
