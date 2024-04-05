package eu.iamgio.quarkdown.function.value

/**
 *
 */
object ValueFactory {
    fun string(raw: String) = StringValue(raw)

    fun number(raw: String) = NumberValue(raw.toIntOrNull() ?: raw.toFloatOrNull() ?: 0)
}
