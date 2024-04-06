package eu.iamgio.quarkdown.function.value

/**
 * Factory of [Value] wrappers from raw string data.
 */
object ValueFactory {
    @FromDynamicType(String::class)
    fun string(raw: String) = StringValue(raw)

    @FromDynamicType(Number::class)
    fun number(raw: String) = NumberValue(raw.toIntOrNull() ?: raw.toFloatOrNull() ?: Double.NaN)
}
