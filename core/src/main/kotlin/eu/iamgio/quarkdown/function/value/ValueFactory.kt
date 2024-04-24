package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainTextNode
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.lexer.Lexer
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
    @FromDynamicType(Iterable::class)
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
     * @param lexer lexer to use to tokenize content
     * @param context context to retrieve the pipeline from, which allows parsing and function expansion
     * @return a new value that wraps the root of the produced AST
     */
    fun markdown(
        lexer: Lexer,
        context: Context,
    ): MarkdownContentValue {
        // Retrieving the pipeline linked to the context.
        val pipeline =
            Pipelines.getAttachedPipeline(context)
                ?: throw IllegalStateException("Context does not have an attached pipeline")

        // Convert string input to parsed AST.
        val root = pipeline.parse(lexer.tokenize())
        // In case the AST contains nested function calls, they are immediately expanded.
        pipeline.expandFunctionCalls(root)

        return MarkdownContentValue(root)
    }

    /**
     * @param raw string input to parse into an AST
     * @param context context to retrieve the pipeline from, which allows tokenization and parsing of the input
     * @return a new value that wraps the root of the produced AST
     */
    @FromDynamicType(MarkdownContent::class, requiresContext = true)
    fun markdown(
        raw: String,
        context: Context,
    ): MarkdownContentValue = this.markdown(context.flavor.lexerFactory.newBlockLexer(raw), context)

    /**
     * @param raw string input that may contain both static values and function calls (e.g. `"2 + 2 is .sum {2} {2}"`)
     * @param context context to retrieve the pipeline from
     * @return the expression (in the previous example: `ComposedExpression(DynamicValue("2 + 2 is "), FunctionCall(sum, 2, 2))`)
     */
    fun expression(
        raw: String,
        context: Context,
    ): Expression? {
        // The content of the argument is tokenized to distinguish static values (string/number/...)
        // from nested function calls, which are also expressions.
        val components =
            this.markdown(
                lexer = context.flavor.lexerFactory.newExpressionLexer(raw, allowBlockFunctionCalls = true),
                context,
            ).unwrappedValue.children

        if (components.isEmpty()) return null

        /**
         * @param node to convert
         * @return an expression that matches the node type
         */
        fun nodeToExpression(node: Node): Expression =
            when (node) {
                is PlainTextNode -> DynamicValue(node.text) // The actual type is determined later.
                is FunctionCallNode -> context.resolveUnchecked(node) // Function existance is checked later.

                else -> throw IllegalArgumentException("Unexpected node $node in expression $raw")
            }

        // Nodes are mapped to expressions.
        return ComposedExpression(expressions = components.map { nodeToExpression(it) })
    }

    /**
     * @param raw string input that may contain both static values and function calls (e.g. `"2 + 2 is .sum {2} {2}"`)
     * @param context context to retrieve the pipeline from
     * @return the result of the expression wrapped in a new [DynamicValue] (in the previous example: `DynamicValue("2 + 2 is 4")`)
     */
    fun dynamic(
        raw: String,
        context: Context,
    ) = DynamicValue(this.expression(raw, context)?.eval()?.unwrappedValue.toString())
}
