package eu.iamgio.quarkdown.function.value

/**
 * Factory of [Value] wrappers from raw string data.
 */
object ValueFactory {
    @FromDynamicType(String::class)
    fun string(raw: String) = StringValue(raw)

    @FromDynamicType(Number::class)
    fun number(raw: String) = (raw.toIntOrNull() ?: raw.toFloatOrNull())?.let { NumberValue(it) }

    @FromDynamicType(Enum::class)
    fun enum(
        raw: String,
        values: Array<Enum<*>>,
    ): EnumValue? = values.find { it.name.equals(raw, ignoreCase = true) }?.let { EnumValue(it) }
}
