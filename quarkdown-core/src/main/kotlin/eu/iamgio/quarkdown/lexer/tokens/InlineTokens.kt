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
 * @see eu.iamgio.quarkdown.ast.base.inline.CriticalContent
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
 * @see eu.iamgio.quarkdown.ast.base.inline.CodeSpan
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
 * @see eu.iamgio.quarkdown.ast.base.inline.LineBreak
 */
class LineBreakToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [Quarkdown](https://github.com/iamgio/quarkdown)
 * ```
 * @see eu.iamgio.quarkdown.ast.base.inline.Link
 */
class LinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <https://github.com/iamgio/quarkdown>
 * ```
 * @see eu.iamgio.quarkdown.ast.base.inline.Link
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
 * @see eu.iamgio.quarkdown.ast.base.inline.Link
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
 * @see eu.iamgio.quarkdown.ast.base.inline.ReferenceLink
 */
class ReferenceLinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ![Label](img.png)
 * ```
 * @see eu.iamgio.quarkdown.ast.base.inline.Image
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
 * @see eu.iamgio.quarkdown.ast.base.inline.ReferenceImage
 */
class ReferenceImageToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <!-- comment -->
 * ```
 * @see eu.iamgio.quarkdown.ast.base.inline.Comment
 */
class CommentToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Text content.
 * @see eu.iamgio.quarkdown.ast.base.inline.Text
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
 * @see eu.iamgio.quarkdown.ast.base.inline.Strong
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
 * @see eu.iamgio.quarkdown.ast.base.inline.Emphasis
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
 * @see eu.iamgio.quarkdown.ast.base.inline.StrongEmphasis
 */
class StrongEmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ~~text~~
 * ```
 * @see eu.iamgio.quarkdown.ast.base.inline.Strikethrough
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
 * @see eu.iamgio.quarkdown.ast.quarkdown.inline.MathSpan
 */
class InlineMathToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
