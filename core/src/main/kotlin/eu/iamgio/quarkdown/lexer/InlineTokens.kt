package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.TokenVisitor

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
 * @see eu.iamgio.quarkdown.ast.PlainText
 */
class PlainTextToken(data: TokenData) : Token(data) {
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
