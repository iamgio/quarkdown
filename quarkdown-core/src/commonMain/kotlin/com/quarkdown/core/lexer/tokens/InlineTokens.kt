package com.quarkdown.core.lexer.tokens

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.scan.inline.quarkdown.TextSymbolReplacement
import com.quarkdown.core.visitor.token.TokenVisitor

// Inline tokens

/**
 * An escaped character.
 * Examples: `\#`, `\>`, ...
 * @param character the escaped character
 */
class EscapeToken(
    data: TokenData,
    val character: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * An entity reference character.
 * Examples: `&nbsp;`, `&amp;`, `&copy;`, '&#35', `&#x22`, ...
 * @param body raw entity body, the ampersand and the optional semicolon excluded
 * @param numeric the decimal or hexadecimal digits of a numeric entity, if any
 */
class EntityToken(
    data: TokenData,
    val body: String,
    val numeric: String?,
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
 * @param content raw code, the backtick runs excluded and untrimmed
 */
class CodeSpanToken(
    data: TokenData,
    val content: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A line break.
 * Example:
 * ```
 * Line 1<space><space>
 * Line 2
 * ```
 * @see com.quarkdown.core.ast.base.inline.LineBreak
 * @see com.quarkdown.core.ast.base.inline.SoftBreak
 * @param isHard whether the break was forced by two spaces or a backslash
 */
class LineBreakToken(
    data: TokenData,
    val isHard: Boolean,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [Quarkdown](https://github.com/iamgio/quarkdown)
 * ```
 * @see com.quarkdown.core.ast.base.inline.Link
 * @param label raw label, brackets excluded
 * @param url raw destination, angle brackets included when it had them
 * @param title raw title including its delimiters, if any
 */
class LinkToken(
    data: TokenData,
    val label: String,
    val url: String,
    val title: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <https://github.com/iamgio/quarkdown>
 * ```
 * @see com.quarkdown.core.ast.base.inline.Link
 * @param url the autolinked URL, the angle brackets excluded
 */
class DiamondAutolinkToken(
    data: TokenData,
    val url: String,
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
 * @param label raw label, brackets excluded
 * @param reference raw reference of the second bracket pair, if any
 */
class ReferenceLinkToken(
    data: TokenData,
    val label: String,
    val reference: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * [^label]
 * ```
 * ```
 * [^label: definition]
 * ```
 * ```
 * [^: definition]
 * ```
 * @see com.quarkdown.core.ast.base.inline.ReferenceFootnote
 * @param label raw footnote label, the `[^` and `]` excluded
 * @param definition raw all-in-one definition, if the reference carries one
 */
class ReferenceFootnoteToken(
    data: TokenData,
    val label: String,
    val definition: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ![Label](img.png)
 * ```
 * @see com.quarkdown.core.ast.base.inline.Image
 * @param label raw label of the delegated link
 * @param url raw destination of the delegated link
 * @param title raw title of the delegated link, if any
 * @param width raw width of the size prefix, if any
 * @param height raw height of the size prefix, if any
 * @param customId raw cross-reference identifier, if any
 */
class ImageToken(
    data: TokenData,
    val label: String,
    val url: String,
    val title: String?,
    val width: String?,
    val height: String?,
    val customId: String?,
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
 * @param label raw label of the delegated reference link
 * @param reference raw reference of the delegated reference link, if any
 * @param width raw width of the size prefix, if any
 * @param height raw height of the size prefix, if any
 * @param customId raw cross-reference identifier, if any
 */
class ReferenceImageToken(
    data: TokenData,
    val label: String,
    val reference: String?,
    val width: String?,
    val height: String?,
    val customId: String?,
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
 * @param children the resolved inner tokens
 */
class StrongToken(
    data: TokenData,
    val children: List<Token>,
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
 * @param children the resolved inner tokens
 */
class EmphasisToken(
    data: TokenData,
    val children: List<Token>,
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
 * @param children the resolved inner tokens
 */
class StrongEmphasisToken(
    data: TokenData,
    val children: List<Token>,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * ~~text~~
 * ```
 * @see com.quarkdown.core.ast.base.inline.Strikethrough
 * @param children the resolved inner tokens
 */
class StrikethroughToken(
    data: TokenData,
    val children: List<Token>,
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
 * @param expression raw math expression, delimiters excluded
 */
class InlineMathToken(
    data: TokenData,
    val expression: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
