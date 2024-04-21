package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.pipeline.Pipelines
import eu.iamgio.quarkdown.util.iterator

/**
 * Factory of [Value] wrappers from raw string data.
 * @see eu.iamgio.quarkdown.function.reflect.FromDynamicType
 * @see eu.iamgio.quarkdown.function.reflect.DynamicValueConverter.convertTo
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
    fun number(raw: String): NumberValue? = (raw.toIntOrNull() ?: raw.toFloatOrNull())?.let { NumberValue(it) }

    /**
     * @param raw raw value to convert to a boolean value.
     *            `true`,`yes` -> `true`,
     *            `false`,`no` -> `false`
     * @return a new boolean value that wraps [raw]'s boolean value, or `null` if [raw] does not represent a boolean
     */
    @FromDynamicType(Boolean::class)
    fun boolean(raw: String): BooleanValue? =
        when (raw.lowercase()) {
            "true", "yes" -> BooleanValue(true)
            "false", "no" -> BooleanValue(false)
            else -> null
        }

    /**
     * @param raw raw value to convert to a range value.
     *            The format is `x..y`, where `x` and `y` are integers that specify start and end of the range.
     *            Both start and end can be omitted to represent an open/infinite value on that end.
     * @return a new range value that wraps the parsed content of [raw].
     *         If the input is invalid, an infinite range is returned
     */
    @FromDynamicType(Range::class)
    fun range(raw: String): ObjectValue<Range> {
        // Matches 'x..y', where both x and y are optional integers.
        val regex = "(\\d+)?..(\\d+)?".toRegex()
        val groups =
            regex.find(raw)?.groupValues
                ?.asSequence()
                ?.iterator(consumeAmount = 1)

        // Start of the range. If null (= not present), the range is open on the left end.
        val start = groups?.next()
        // End of the range. If null (= not present), the range is open on the right end.
        val end = groups?.next()

        // Indexes start from 1:
        // 2..5 maps to Range(1, 4)
        val range =
            Range(
                start?.toIntOrNull()?.minus(1),
                end?.toIntOrNull()?.minus(1),
            )

        return ObjectValue(range)
    }

    /**
     * @param raw raw value to convert to an enum value
     * @param values enum values pool to pick the output value from
     * @return the value whose name matches (ignoring case) with [raw], or `null` if no match is found
     */
    @FromDynamicType(Enum::class)
    fun enum(
        raw: String,
        values: Array<Enum<*>>,
    ): EnumValue? =
        values.find { it.name.equals(raw, ignoreCase = true) }
            ?.let { EnumValue(it) }

    /**
     * @param raw string input to parse into an AST
     * @param context context to retrieve the pipeline from, which allows tokenization and parsing of the input
     * @return a new value that wraps the root of the produced AST
     */
    @FromDynamicType(MarkdownContent::class, requiresContext = true)
    fun markdown(
        raw: String,
        context: Context,
    ): MarkdownContentValue {
        // Retrieving the pipeline linked to the context.
        val pipeline =
            Pipelines.getAttachedPipeline(context)
                ?: throw IllegalStateException("Context does not have an attached pipeline")

        // Convert string input to parsed AST.
        val root = pipeline.parse(pipeline.tokenize(raw))
        // In case the AST contains nested function calls, they are immediately expanded.
        pipeline.expandFunctionCalls(root)

        return MarkdownContentValue(root)
    }
}
