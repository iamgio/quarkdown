package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.InlineMarkdownContent
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainTextNode
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.SizeUnit
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.function.error.InvalidLambdaArgumentCountException
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.pipeline.Pipelines
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.replace

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
     * @see iterable
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
     * @param raw raw value to convert to a size value.
     *            The format is `Xunit`, where `X` is a number (integer or floating point)
     *            and `unit` is one of the following: `px`, `pt`, `cm`, `mm`, `in`. If not specified, `px` is assumed.
     * @return a new size value that wraps the parsed content of [raw].
     * @throws IllegalArgumentException if the value is an invalid size
     */
    @FromDynamicType(Size::class)
    fun size(raw: String): ObjectValue<Size> {
        // Matches value and unit, e.g. 10px, 12.5cm, 3in.
        val regex = "^(\\d+(?:\\.\\d+)?)(px|pt|cm|mm|in)?$".toRegex()
        val groups = regex.find(raw)?.groupValues?.asSequence()?.iterator(consumeAmount = 1)

        // The value, which is required.
        val value = groups?.next()?.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid size: $raw")

        // The unit, which is optional and defaults to pixels.
        val rawUnit = groups.next()
        val unit = SizeUnit.values().find { it.name.equals(rawUnit, ignoreCase = true) } ?: SizeUnit.PX

        return ObjectValue(Size(value, unit))
    }

    /**
     * @param raw raw value to convert to a collection of sizes.
     * @see size for the treatment of each size
     * @throws IllegalArgumentException if the raw value contains a different amount of sizes than 1, 2 or 4,
     *                                  of if any of those values is an invalid size
     */
    @FromDynamicType(Sizes::class)
    fun sizes(raw: String): ObjectValue<Sizes> {
        val parts = raw.split("\\s+".toRegex())
        val iterator = parts.iterator()

        return ObjectValue(
            when (parts.size) {
                // Single size: all sides are the same.
                1 -> Sizes(all = size(iterator.next()).unwrappedValue)
                // Two sizes: vertical and horizontal.
                2 ->
                    Sizes(
                        vertical = size(iterator.next()).unwrappedValue,
                        horizontal = size(iterator.next()).unwrappedValue,
                    )
                // Four sizes: top, right, bottom, left.
                4 ->
                    Sizes(
                        top = size(iterator.next()).unwrappedValue,
                        right = size(iterator.next()).unwrappedValue,
                        bottom = size(iterator.next()).unwrappedValue,
                        left = size(iterator.next()).unwrappedValue,
                    )

                else -> throw IllegalArgumentException("Invalid top-right-bottom-left sizes: $raw")
            },
        )
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
     * @param expandFunctionCalls whether enqueued function calls should be expanded instantly
     * @return a new value that wraps the root of the produced AST
     */
    fun markdown(
        lexer: Lexer,
        context: Context,
        expandFunctionCalls: Boolean,
    ): MarkdownContentValue {
        // Retrieving the pipeline linked to the context.
        val pipeline =
            Pipelines.getAttachedPipeline(context)
                ?: throw IllegalStateException("Context does not have an attached pipeline")

        // Convert string input to parsed AST.
        val root = pipeline.parse(lexer.tokenize())

        if (expandFunctionCalls) {
            // In case the AST contains nested function calls, they are immediately expanded.
            // If expandF
            pipeline.expandFunctionCalls(root)
        }

        return MarkdownContentValue(MarkdownContent(root.children))
    }

    /**
     * @param raw string input to parse into a sub-AST
     * @param context context to retrieve the pipeline from, which allows tokenization and parsing of the input
     * @return a new value that wraps the root of the produced AST, containing both block and inline content
     */
    @FromDynamicType(MarkdownContent::class, requiresContext = true)
    fun blockMarkdown(
        raw: String,
        context: Context,
    ): MarkdownContentValue =
        this.markdown(
            context.flavor.lexerFactory.newBlockLexer(raw),
            context,
            expandFunctionCalls = true,
        )

    /**
     * @param raw string input to parse into a sub-AST
     * @param context context to retrieve the pipeline from, which allows tokenization and parsing of the input
     * @return a new value that wraps the root of the produced AST, containing inline content only
     */
    @FromDynamicType(InlineMarkdownContent::class, requiresContext = true)
    fun inlineMarkdown(
        raw: String,
        context: Context,
    ): InlineMarkdownContentValue =
        this.markdown(
            context.flavor.lexerFactory.newInlineLexer(raw),
            context,
            expandFunctionCalls = true,
        ).asInline()

    /**
     * Evaluates an expression from a raw string input.
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
                expandFunctionCalls = false,
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
     * @param raw string input to parse the expression from
     * @param context context to retrieve the pipeline from
     * @return a new [IterableValue] from the raw expression. It can also be a [Range].
     * @see range
     */
    @Suppress("UNCHECKED_CAST")
    @FromDynamicType(Iterable::class, requiresContext = true)
    fun <T : OutputValue<*>> iterable(
        raw: String,
        context: Context,
    ): IterableValue<T> {
        // A range is a suitable numeric iterable value.
        val range = this.range(raw)
        if (!range.unwrappedValue.isInfinite) {
            return range.unwrappedValue.toCollection() as IterableValue<T>
        }

        // The expression is evaluated into an iterable.
        val value = this.expression(raw, context)?.eval() ?: return OrderedCollectionValue(emptyList())

        return value as? IterableValue<T>
            ?: throw IllegalStateException("$raw is not a suitable iterable (found: $value)")
    }

    /**
     * Converts a raw string input to a lambda value.
     * Lambda example: `param1 param2 => Hello, <<param1>> and <<param2>>!`
     * @param raw string input to parse the lambda from
     * @return a new [LambdaValue] from the raw input
     */
    @FromDynamicType(Lambda::class)
    fun lambda(raw: String): LambdaValue {
        // The header is the part before the delimiter.
        // The header contains the sequence of lambda parameters.
        // If no header is present, the lambda has no parameters.
        val headerDelimiter = ":"
        // Matches a sequence of words separated by spaces or tabs, followed by the delimiter.
        val headerRegex = "^\\s*(\\w+[ \\t]*)*(?=$headerDelimiter)".toRegex()
        val header = headerRegex.find(raw)?.value ?: ""
        val parameters = header.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }

        // The body is the part after the delimiter,
        // which is the actual content of the lambda.
        // The body may contain placeholders wrapped in <<>> that will be replaced with actual arguments upon invocation.
        val body =
            if (header.isEmpty()) {
                raw
            } else {
                // Strip the header and delimiter.
                raw.substring(raw.indexOf(headerDelimiter) + headerDelimiter.length)
                    .trimStart()
            }

        return LambdaValue(
            Lambda { arguments ->
                // Check if the amount of arguments matches the amount of expected parameters.
                // In case parameters are not present, placeholders are automatically set to
                // <<1>>, <<2>>, etc., similarly to Kotlin's 'it' argument.
                if (arguments.size != parameters.size && parameters.isNotEmpty()) {
                    throw InvalidLambdaArgumentCountException(parameters.size, arguments.size)
                }

                val builder = StringBuilder(body)
                // Placeholder replacement.
                arguments.forEachIndexed { index, argument ->
                    // If no parameters are present, placeholders are automatically set to <<1>>, <<2>>, etc.
                    // Otherwise, the placeholder is set to the parameter name.
                    val placeholder = parameters.getOrNull(index) ?: (index + 1).toString()
                    // Replace the placeholder with the actual argument value.
                    builder.replace("<<$placeholder>>", argument.unwrappedValue.toString())
                }
                builder.toString()
            },
        )
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
