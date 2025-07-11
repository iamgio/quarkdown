package com.quarkdown.core.lexer.tokens

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.patterns.TextSymbolReplacement
import com.quarkdown.core.visitor.token.TokenVisitor

// Inline tokens

/**
 * An escaped character.
 * Examples: `\#`, `\>`, ...
 */
class EscapeToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * An entity reference character.
 * Examples: `&nbsp;`, `&amp;`, `&copy;`, '&#35', `&#x22`, ...
 */
class EntityToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A character that requires special treatment during the rendering stage.
 * Examples: `&`, `<`, `>`, `"`, `'`, ...
 * @see com.quarkdown.core.ast.base.inline.CriticalContent
 */
class CriticalContentToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.CodeSpan
 */
class CodeSpanToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A soft line break.
 * Example:
 * ```
 * Line 1<space><space>
 * Line 2
 * ```
 * @see com.quarkdown.core.ast.base.inline.LineBreak
 */
class LineBreakToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [Quarkdown](https://github.com/iamgio/quarkdown)
 * ```
 * @see com.quarkdown.core.ast.base.inline.Link
 */
class LinkToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <https://github.com/iamgio/quarkdown>
 * ```
 * @see com.quarkdown.core.ast.base.inline.Link
 */
class DiamondAutolinkToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * GFM extension.
 * Example:
 * ```
 * https://github.com/iamgio/quarkdown
 * ```
 * @see com.quarkdown.core.ast.base.inline.Link
 */
class UrlAutolinkToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.ReferenceLink
 */
class ReferenceLinkToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [^label]
 * ```
 * @see com.quarkdown.core.ast.base.inline.ReferenceFootnote
 */
class ReferenceFootnoteToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ![Label](img.png)
 * ```
 * @see com.quarkdown.core.ast.base.inline.Image
 */
class ImageToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.ReferenceImage
 */
class ReferenceImageToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <!-- comment -->
 * ```
 * @see com.quarkdown.core.ast.base.inline.Comment
 */
class CommentToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Text content.
 * @see com.quarkdown.core.ast.base.inline.Text
 */
class PlainTextToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A sequence of characters that is replaced with a symbol (e.g. `(C)` -> ©).
 * This is a Quarkdown extension.
 * @param symbol symbol type
 * @see com.quarkdown.core.ast.quarkdown.inline.TextSymbol
 */
class TextSymbolToken(
    data: TokenData,
    val symbol: TextSymbolReplacement,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.Strong
 */
class StrongToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.Emphasis
 */
class EmphasisToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.base.inline.StrongEmphasis
 */
class StrongEmphasisToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ~~text~~
 * ```
 * @see com.quarkdown.core.ast.base.inline.Strikethrough
 */
class StrikethroughToken(
    data: TokenData,
) : Token(data) {
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
 * @see com.quarkdown.core.ast.quarkdown.inline.MathSpan
 */
class InlineMathToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
