package com.quarkdown.lsp.highlight.function

import com.quarkdown.core.function.toQuarkdownNamingFormat
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.lsp.highlight.TokenType

/**
 * Utility for determining the token type of Quarkdown values in their raw string form.
 * For instance, this is used by [FunctionCallTokensSupplier] to categorize the content of function call arguments.
 */
object ValueQualifier {
    /**
     * Identifies the semantic token type of the given text.
     * Supported types are, in order of precedence:
     * - Boolean ([ValueFactory.boolean])
     * - Number ([ValueFactory.number])
     * - Sizes ([ValueFactory.sizes])
     * - Range ([ValueFactory.range])
     * - Enum
     * @param text the text to categorize, e.g. the content of a function call argument
     * @return the appropriate [TokenType] for the text, or `null` if the text doesn't match any known value type
     */
    fun getTokenType(text: String): TokenType? =
        when {
            text.isBlank() -> null
            isType { boolean(text) } -> TokenType.BOOLEAN
            isType { number(text) } -> TokenType.NUMBER
            isType { sizes(text) } -> TokenType.SIZE
            isType { range(text) } -> TokenType.RANGE
            isEnum(text) -> TokenType.ENUM
            else -> null
        }

    /**
     * Checks if the text represents a specific value type.
     * @param conversion a function that validates the text against a specific value type
     * @return whether the given text represents the specified value type
     */
    private fun isType(conversion: ValueFactory.() -> Value<*>): Boolean = ValueFactory.tryOrNull(conversion) != null

    /**
     * Checks if the text is a valid enum name in Quarkdown format.
     * An enum name is considered valid if it matches the Quarkdown naming format and contains only word characters.
     * @param text the text to check
     * @return whether if the text is a valid enum name
     */
    private fun isEnum(text: String): Boolean = text == text.toQuarkdownNamingFormat() && Regex("\\W") !in text
}
