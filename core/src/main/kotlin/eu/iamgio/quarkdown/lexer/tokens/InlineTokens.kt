package eu.iamgio.quarkdown.lexer.tokens

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.patterns.TextSymbolReplacement
import eu.iamgio.quarkdown.visitor.token.TokenVisitor

// Inline tokens

/**
 * An escaped character.
 * Examples: `\#`, `\>`, ...
 */
class EscapeToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * An entity reference character.
 * Examples: `&nbsp;`, `&amp;`, `&copy;`, '&#35', `&#x22`, ...
 */
class EntityToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A character that requires special treatment during the rendering stage.
 * Examples: `&`, `<`, `>`, `"`, `'`, ...
 */
class CriticalContentToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * `code`
 * ```
 * ```
 * ````code````
 * ```
 */
class CodeSpanToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A soft line break.
 * Example:
 * ```
 * Line 1<space><space>
 * Line 2
 * ```
 */
class LineBreakToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [Quarkdown](https://github.com/iamgio/quarkdown)
 * ```
 */
class LinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <https://github.com/iamgio/quarkdown>
 * ```
 */
class DiamondAutolinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * GFM extension.
 * Example:
 * ```
 * https://github.com/iamgio/quarkdown
 * ```
 */
class UrlAutolinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * [text][label]
 * ```
 * ```
 * [label][]
 * ```
 * ```
 * [label]
 * ```
 */
class ReferenceLinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ![Label](img.png)
 * ```
 */
class ImageToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * ![text][label]
 * ```
 * ```
 * ![label][]
 * ```
 * ```
 * ![label]
 * ```
 */
class ReferenceImageToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <!-- comment -->
 * ```
 */
class CommentToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Text content.
 * @see eu.iamgio.quarkdown.ast.Text
 */
class PlainTextToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A sequence of characters that is replaced with a symbol (e.g. `(C)` -> ©).
 * This is a Quarkdown extension.
 * @param symbol symbol type
 * @see eu.iamgio.quarkdown.ast.quarkdown.inline.TextSymbol
 */
class TextSymbolToken(data: TokenData, val symbol: TextSymbolReplacement) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

// Emphasis

/**
 * Examples:
 * ```
 * **strong**
 * ```
 * ```
 * __strong__
 * ```
 * @see eu.iamgio.quarkdown.ast.Strong
 */
class StrongToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * *emphasis*
 * ```
 * ```
 * _emphasis_
 * ```
 * @see eu.iamgio.quarkdown.ast.Emphasis
 */
class EmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * ***emphasis***
 * ```
 * ```
 * ___emphasis___
 * ```
 * @see eu.iamgio.quarkdown.ast.StrongEmphasis
 */
class StrongEmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ~~text~~
 * ```
 * @see eu.iamgio.quarkdown.ast.Strikethrough
 */
class StrikethroughToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

// Quarkdown extensions

/**
 * A one-line fenced block that contains a TeX expression.
 * If it's isolated, then it's a [OnelineMathToken].
 * This is a Quarkdown extension.
 *
 * Example:
 * $ LaTeX expression $
 * @see eu.iamgio.quarkdown.ast.MathSpan
 */
class InlineMathToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
