package eu.iamgio.quarkdown.function.value.factory

import eu.iamgio.quarkdown.ast.InlineMarkdownContent
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.list.ListBlock
import eu.iamgio.quarkdown.ast.base.block.list.ListItem
import eu.iamgio.quarkdown.ast.base.inline.PlainTextNode
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.function.error.internal.InvalidExpressionEvalException
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.InlineMarkdownContentValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.LambdaValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.UnorderedCollectionValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.data.EvaluableString
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.data.LambdaParameter
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.quarkdownName
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.misc.color.Color
import eu.iamgio.quarkdown.misc.color.decoder.decode
import eu.iamgio.quarkdown.pipeline.error.UnattachedPipelineException
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.toPlainText

/**
 * Suffix that marks a lambda parameter as optional.
 * @see ValueFactory.lambda
 */
private const val LAMBDA_OPTIONAL_PARAMETER_SUFFIX = '?'

/**
 * Prefix that forces a generic expression to be parsed as a lambda block.
 * @see ValueFactory.expression
 */
private const val EXPRESSION_FORCE_LAMBDA_PREFIX = "@lambda "

/**
 * Factory of [Value] wrappers from raw data.
 * @see eu.iamgio.quarkdown.function.reflect.FromDynamicType
 * @see eu.iamgio.quarkdown.function.reflect.DynamicValueConverter.convertTo
 */
object ValueFactory {
    /**
     * @param raw raw value to convert to a string value
     * @return a new string value that wraps [raw]
     */
    @FromDynamicType(String::class)
    fun string(raw: Any) = StringValue(raw.toString())

    /**
     * @param raw raw value to convert to a number value
     * @return a new number value that wraps [raw]'s integer (if possible) or float value
     * @throws IllegalRawValueException if [raw] is not a valid numeric value
     */
    @FromDynamicType(Number::class)
    fun number(raw: Any): NumberValue =
        when (raw) {
            is Number -> NumberValue(raw)
            else ->
                raw
                    .toString()
                    .let { it.toIntOrNull() ?: it.toFloatOrNull() }
                    ?.let { NumberValue(it) }
                    ?: throw IllegalRawValueException("Not a numeric value", raw)
        }

    /**
     * @param raw raw value to convert to a boolean value.
     *            `true`,`yes` -> `true`,
     *            `false`,`no` -> `false`
     * @return a new boolean value that wraps [raw]'s boolean value, or `null` if [raw] does not represent a boolean
     * @throws IllegalRawValueException if [raw] is not a valid boolean value
     */
    @FromDynamicType(Boolean::class)
    fun boolean(raw: Any): BooleanValue =
        when (raw) {
            is Boolean -> BooleanValue(raw)
            else ->
                when (raw.toString().lowercase()) {
                    "true", "yes" -> BooleanValue(true)
                    "false", "no" -> BooleanValue(false)
                    else -> throw IllegalRawValueException("Not a valid boolean value", raw)
                }
        }

    /**
     * @param raw raw value to convert to a range value.
     *            The format is `x..y`, where `x` and `y` are integers that specify start and end of the range.
     *            Both start and end can be omitted to represent an open/infinite value on that end.
     * @return a new range value that wraps the parsed content of [raw]
     * @throws IllegalRawValueException if the value is an invalid range
     * @see iterable
     */
    @FromDynamicType(Range::class)
    fun range(raw: Any): ObjectValue<Range> {
        if (raw is Range) return ObjectValue(raw)

        val rawString = raw.toString()

        // Matches 'x..y', where both x and y are optional integers.
        val regex = "(\\d+)?..(\\d+)?".toRegex()

        // If the raw value does not represent a range, an error is thrown.
        if (!regex.matches(rawString)) {
            throw IllegalRawValueException("Invalid range", raw)
        }

        val groups =
            regex
                .find(rawString)
                ?.groupValues
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
                start?.toIntOrNull(),
                end?.toIntOrNull(),
            )

        return ObjectValue(range)
    }

    /**
     * @param raw raw value to convert to a size value.
     *            The format is `Xunit`, where `X` is a number (integer or floating point)
     *            and `unit` is one of the following: `px`, `pt`, `cm`, `mm`, `in`. If not specified, `px` is assumed.
     * @return a new size value that wraps the parsed content of [raw].
     * @throws IllegalRawValueException if the value is an invalid size
     */
    @FromDynamicType(Size::class)
    fun size(raw: Any): ObjectValue<Size> {
        if (raw is Size) return ObjectValue(raw)

        // All possible unit symbols.
        val symbolsRegex = Size.Unit.entries.joinToString("|") { it.symbol }
        // Matches value and unit, e.g. 10px, 12.5cm, 3in.
        val regex = "^(\\d+(?:\\.\\d+)?)($symbolsRegex)?$".toRegex()
        val groups =
            regex
                .find(raw.toString())
                ?.groupValues
                ?.asSequence()
                ?.iterator(consumeAmount = 1)

        // The value, which is required.
        val value = groups?.next()?.toDoubleOrNull() ?: throw IllegalRawValueException("Invalid size", raw)

        // The unit, which is optional and defaults to pixels.
        val rawUnit = groups.next()
        val unit = Size.Unit.entries.find { it.symbol.equals(rawUnit, ignoreCase = true) } ?: Size.Unit.PIXELS

        return ObjectValue(Size(value, unit))
    }

    /**
     * @param raw raw value to convert to a collection of sizes.
     * @see size for the treatment of each size
     * @throws IllegalRawValueException if the raw value contains a different amount of sizes than 1, 2 or 4,
     *                                  of if any of those values is an invalid size
     */
    @FromDynamicType(Sizes::class)
    fun sizes(raw: Any): ObjectValue<Sizes> {
        if (raw is Sizes) return ObjectValue(raw)

        val parts = raw.toString().split("\\s+".toRegex())
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

                else -> throw IllegalRawValueException("Invalid top-right-bottom-left sizes", raw)
            },
        )
    }

    /**
     * @param raw raw value to convert to a color value, case-insensitive.
     *            Can be a hex value starting by `#` (e.g. `#FF0000`) or a color name (e.g. `red`).
     * @return a new color value that wraps the parsed content of [raw]
     * @throws IllegalRawValueException if the value is an invalid color
     */
    @FromDynamicType(Color::class)
    fun color(raw: Any): ObjectValue<Color> {
        if (raw is Color) return ObjectValue(raw)

        return Color.decode(raw.toString())?.let(::ObjectValue)
            ?: throw IllegalRawValueException("Not a valid color", raw)
    }

    /**
     * @param raw raw value to convert to an enum value
     * @param values enum values pool to pick the output value from
     * @return the value whose name matches (ignoring case and with `_`s removed) with [raw], or `null` if no match is found
     */
    @FromDynamicType(Enum::class)
    fun enum(
        raw: Any,
        values: Array<Enum<*>>,
    ): EnumValue? =
        when (raw) {
            is Enum<*> -> EnumValue(raw)
            else ->
                values
                    .find { it.quarkdownName.equals(raw.toString(), ignoreCase = true) }
                    ?.let { EnumValue(it) }
        }

    /**
     * Generates an [EvaluableString].
     * Contrary to [String], an [EvaluableString] natively supports function calls and scripting evaluation.
     * @param raw raw value to convert to a string expression
     * @param context context to evaluate the raw value in
     * @return a new string expression value that wraps the evaluated content of [raw]
     * @see eval for the evaluation process
     */
    @FromDynamicType(EvaluableString::class, requiresContext = true)
    fun evaluableString(
        raw: Any,
        context: Context,
    ): ObjectValue<EvaluableString> =
        ObjectValue(
            EvaluableString(
                eval(raw.toString(), context).unwrappedValue.toString(),
            ),
        )

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
        val pipeline = context.attachedPipeline ?: throw UnattachedPipelineException()

        context as MutableContext

        // Convert string input to parsed AST.

        fun parse() = pipeline.parse(lexer.tokenize(), context)

        // If function calls should not be expanded, then they are not enqueued.
        val root = if (expandFunctionCalls) parse() else context.lockFunctionCallEnqueuing { parse() }

        if (expandFunctionCalls) {
            // In case the AST contains nested function calls, they are immediately expanded.
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
        raw: Any,
        context: Context,
    ): MarkdownContentValue =
        when (raw) {
            is MarkdownContent -> MarkdownContentValue(raw)
            else ->
                markdown(
                    context.flavor.lexerFactory.newBlockLexer(raw.toString()),
                    context,
                    expandFunctionCalls = true,
                )
        }

    /**
     * @param raw string input to parse into a sub-AST
     * @param context context to retrieve the pipeline from, which allows tokenization and parsing of the input
     * @return a new value that wraps the root of the produced AST, containing inline content only
     */
    @FromDynamicType(InlineMarkdownContent::class, requiresContext = true)
    fun inlineMarkdown(
        raw: Any,
        context: Context,
    ): InlineMarkdownContentValue =
        when (raw) {
            is InlineMarkdownContent -> InlineMarkdownContentValue(raw)
            else ->
                markdown(
                    context.flavor.lexerFactory.newInlineLexer(raw.toString()),
                    context,
                    expandFunctionCalls = true,
                ).asInline()
        }

    /**
     * Converts a Markdown list to an [OrderedCollectionValue] iterable.
     * The text of each list item is stored as a [DynamicValue], hence it can be adapted to any type at invocation time.
     * Currently, it's not supported to have nested lists or node values.
     * Example:
     * ```
     * - A
     * - B
     * - C
     * ```
     * is converted to an iterable containing `A`, `B` and `C`.
     * @param raw Markdown string input that represents a list to parse the expression from
     * @param context context to retrieve the pipeline from
     * @return a new [OrderedCollectionValue] from the raw expression, or `null` if the raw input is not a list
     * @see iterable
     */
    private fun markdownListToIterable(
        raw: String,
        context: Context,
    ): IterableValue<*>? {
        val content = blockMarkdown(raw, context).unwrappedValue
        val list = content.children.singleOrNull() as? ListBlock ?: return null

        val items =
            list.children
                .asSequence()
                .filterIsInstance<ListItem>()
                .map { it.children.toPlainText() }
                .map(::DynamicValue)

        return OrderedCollectionValue(items.toList())
    }

    /**
     * @param raw string input to parse the expression from
     * @param context context to retrieve the pipeline from
     * @return a new [IterableValue] from the raw expression. It can also be a [Range].
     * @throws IllegalRawValueException if [raw] cannot be converted to an iterable
     * @see range
     */
    @Suppress("UNCHECKED_CAST")
    @FromDynamicType(Iterable::class, requiresContext = true)
    fun iterable(
        raw: Any,
        context: Context,
    ): IterableValue<*> {
        when (raw) {
            is Range -> return raw.toCollection()
            is List<*> -> return OrderedCollectionValue(raw as List<OutputValue<*>>)
            is Set<*> -> return UnorderedCollectionValue(raw as Set<OutputValue<*>>)
            is Iterable<*> -> return GeneralCollectionValue(raw as Iterable<OutputValue<*>>)
        }

        // A range is a suitable numeric iterable value.
        try {
            val range = range(raw)
            return range.unwrappedValue.toCollection()
        } catch (ignored: IllegalRawValueException) {
            // The raw value is not a range.
        }

        val rawString = raw.toString()

        // The expression is evaluated into an iterable.
        val value = expression(rawString, context)?.eval() ?: return OrderedCollectionValue(emptyList())

        return value as? IterableValue<*>
            ?: (value as? DictionaryValue<*>)?.adapt() // A dictionary is an iterable of key-value pairs.
            ?: markdownListToIterable(rawString, context) // A Markdown list is a valid iterable.
            ?: throw IllegalRawValueException("Not a suitable iterable (found: $value)", raw)
    }

    /**
     * Converts a raw string input to a dictionary value.
     * A dictionary is a collection of key-value pairs,
     * where keys are strings and values can be expressed in two ways:
     * - Inline, in the format `- key: value`.
     * - Nested dictionaries, in the format:
     *   ```
     *   - key
     *     - value
     *   ```
     *
     * Dictionary example, of type `DictionaryValue<DictionaryValue<StringValue>>`:
     * ```
     * - keyA:
     *   - keyAA: valueAA
     * - keyB
     *   - keyBA: valueBA
     *   - keyBB: valueBB
     * - keyC:
     *   - keyCA: valueCA
     * ```
     * @param raw string input to parse the dictionary from
     * @param context context to retrieve the pipeline from
     * @return a new [DictionaryValue] from the raw input
     * @throws IllegalRawValueException if the raw input cannot be converted to a dictionary
     * @see MarkdownListToDictionary
     */
    @Suppress("UNCHECKED_CAST")
    @FromDynamicType(Map::class, requiresContext = true)
    fun dictionary(
        raw: Any,
        context: Context,
    ): DictionaryValue<*> {
        (raw as? Map<String, OutputValue<*>>)?.let { return DictionaryValue(it.toMutableMap()) }

        val content = blockMarkdown(raw, context).unwrappedValue
        val list =
            content.children.singleOrNull { it !is Newline } as? ListBlock
                ?: throw IllegalRawValueException("Not a dictionary (the only element must be a Markdown list)", raw)

        fun convert(list: ListBlock): DictionaryValue<*> =
            MarkdownListToDictionary(
                list,
                // Node values are currently unsupported as dictionary values.
                // Here we give back the raw string as a fallback in case a node is met.
                inlineValueMapper = { eval(it, context, fallback = { it.wrappedAsValue() }) },
                nestedValueMapper = { convert(it) },
                nothingValueMapper = { DictionaryValue(mutableMapOf()) },
            ).convert()

        return convert(list)
    }

    /**
     * Converts a raw string input to a lambda value.
     * Lambda example: `param1 param2 => Hello, .param1 and .param2!`
     * @param raw string input to parse the lambda from
     * @return a new [LambdaValue] from the raw input
     */
    @FromDynamicType(Lambda::class, requiresContext = true)
    fun lambda(
        raw: Any,
        context: Context,
    ): LambdaValue {
        if (raw is Lambda) return LambdaValue(raw)

        val rawString = raw.toString()

        // The header is the part before the delimiter.
        // The header contains the sequence of lambda parameters.
        // If no header is present, the lambda has no parameters.
        val headerDelimiter = ":"
        // Matches a sequence of words separated by spaces or tabs,
        // followed by an optional '?' (makes it optional),
        // followed by the delimiter.
        val headerRegex = "^\\s*(\\w+\\??[ \\t]*)*(?=$headerDelimiter)".toRegex()
        val header = headerRegex.find(rawString)?.value ?: ""

        // The parameters are extracted from the header.
        val parameters: List<LambdaParameter> =
            header
                .trim()
                .split("\\s+".toRegex())
                .asSequence()
                .filter { it.isNotBlank() }
                .map { parameterName ->
                    // If a parameter ends with '?', it is optional.
                    val isOptional = parameterName.endsWith(LAMBDA_OPTIONAL_PARAMETER_SUFFIX)
                    // The '?' is stripped from the parameter name.
                    val name = if (isOptional) parameterName.dropLast(1) else parameterName

                    LambdaParameter(name, isOptional)
                }.toList()

        // The body is the part after the delimiter,
        // which is the actual content of the lambda.
        // The body may contain placeholders wrapped in <<>> that will be replaced with actual arguments upon invocation.
        val body =
            if (header.isEmpty()) {
                rawString
            } else {
                // Strip the header and delimiter.
                rawString
                    .substring(rawString.indexOf(headerDelimiter) + headerDelimiter.length)
                    .trimStart()
            }

        return LambdaValue(
            Lambda(context, explicitParameters = parameters) { _, newContext ->
                // The body (as a raw code snippet) is evaluated in the context of the lambda
                // which is a fork of the original one.
                // Parameters-arguments count match is checked later.
                // Here we assume they match is correct.
                // Check Lambda#invokeDynamic for more details.
                eval(body, newContext)
            },
        )
    }

    /**
     * Evaluates a dynamic expression from a raw string input.
     * Special case: if the raw string starts with `@lambda`, the content is parsed as a [lambda] value.
     * @param raw string input that may contain both static values and function calls (e.g. `"2 + 2 is .sum {2} {2}"`)
     * @param context context to retrieve the pipeline from
     * @return the expression (in the previous example: `ComposedExpression(DynamicValue("2 + 2 is "), FunctionCall(sum, 2, 2))`)
     */
    fun expression(
        raw: String,
        context: Context,
    ): Expression? {
        if (raw.isEmpty()) return DynamicValue("")

        // If the raw string starts with `@lambda`, the content is force-parsed as a lambda.
        if (raw.startsWith(EXPRESSION_FORCE_LAMBDA_PREFIX)) {
            val lambdaRaw = raw.removePrefix(EXPRESSION_FORCE_LAMBDA_PREFIX)
            return lambda(lambdaRaw, context)
        }

        // The content of the argument is tokenized to distinguish static values (string/number/...)
        // from nested function calls, which are also expressions.
        val components =
            markdown(
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
        return ComposedExpression(expressions = components.map(::nodeToExpression))
    }

    /**
     * Evaluates an expression from a raw string input.
     * @param raw string input that may contain both static values and function calls (e.g. `"2 + 2 is .sum {2} {2}"`)
     * @param context context to retrieve the pipeline from
     * @param fallback value to return if the expression is invalid or an error occurs during the evaluation.
     * A common example of an invalid expression evaluation is when a [NodeValue] is present in a [ComposedExpression], hence the expected output is a pure Markdown output node.
     * The fallback function defaults to returning a block-Markdown content node.
     * @return the result of the evaluation of the expression (in the previous example: `ComposedExpression(DynamicValue("2 + 2 is "), FunctionCall(sum, 2, 2))`),
     *         or the result of the fallback function if the expression is invalid
     */
    fun eval(
        raw: String,
        context: Context,
        fallback: () -> OutputValue<*> = { blockMarkdown(raw, context).asNodeValue() },
    ): OutputValue<*> {
        val expression = expression(raw, context) ?: return fallback()

        return try {
            expression.eval().let {
                it as? OutputValue<*>
                    ?: throw IllegalStateException("The result of the expression is not a suitable OutputValue: $it")
            }
        } catch (e: InvalidExpressionEvalException) {
            // All enqueued function calls are invalidated and discarded.
            (context as? MutableContext)?.dequeueAllFunctionCalls()
            // The fallback function is called to provide a default value.
            // The default behavior is Markdown parsing.
            fallback()
        }
    }
}
