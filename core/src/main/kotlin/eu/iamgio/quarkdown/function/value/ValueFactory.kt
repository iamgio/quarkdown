package eu.iamgio.quarkdown.function.value

/**
 * Factory of [Value] wrappers from raw string data.
 */
object ValueFactory {
    fun string(raw: String) = StringValue(raw)

    fun number(raw: String) = NumberValue(raw.toIntOrNull() ?: raw.toFloatOrNull() ?: 0)
}
