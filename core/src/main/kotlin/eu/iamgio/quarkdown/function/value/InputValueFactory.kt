package eu.iamgio.quarkdown.function.value

/**
 *
 */
object InputValueFactory {
    fun string(raw: String) = Value(raw, StringType)

    fun number(raw: String) = Value(raw.toIntOrNull() ?: raw.toFloatOrNull() ?: 0, NumberType)
}
