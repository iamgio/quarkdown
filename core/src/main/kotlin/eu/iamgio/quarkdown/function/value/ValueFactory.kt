package eu.iamgio.quarkdown.function.value

/**
 * Factory of [Value] wrappers from raw string data.
 * @see DynamicInputValue.convertTo
 */
object ValueFactory {
    /**
     * @param raw raw value to convert to a string value
     * @return a new string value that wraps [raw]
     */
    @FromDynamicType(String::class)
    fun string(raw: String) = StringValue(raw)

    /**
     * @param raw raw value to convert to a number value
     * @return a new number value that wraps [raw]'s integer (if possible) or float value, or `null` if [raw] is not numeric
     */
    @FromDynamicType(Number::class)
    fun number(raw: String) = (raw.toIntOrNull() ?: raw.toFloatOrNull())?.let { NumberValue(it) }

    /**
     * @param raw raw value to convert to an enum value
     * @param values enum values pool to pick the output value from
     * @return the value whose name matches (ignoring case) with [raw], or `null` if no match is found
     */
    @FromDynamicType(Enum::class)
    fun enum(
        raw: String,
        values: Array<Enum<*>>,
    ): EnumValue? = values.find { it.name.equals(raw, ignoreCase = true) }?.let { EnumValue(it) }
}
