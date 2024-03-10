package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

// Inline tokens

/**
 * An escaped character.
 * Example:
 * ```
 * \#
 * ```
 */
class EscapeToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
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
class InlineCodeToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
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
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class PunctuationToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class BlockSkipToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class AnyPunctuationToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Example:
 * ```
 * <https://github.com/iamgio/quarkdown>
 * ```
 */
class AutolinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Example:
 * ```
 * [Quarkdown](https://github.com/iamgio/quarkdown)
 * ```
 */
class LinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Example:
 * ```
 * [text][label]
 * ```
 */
class ReferenceLinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Example:
 * ```
 * [label][]
 * ```
 */
class CollapsedReferenceLinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class ReferenceLinkSearchToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Example:
 * ```
 * <!-- comment -->
 * ```
 */
class CommentToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Text content.
 */
class PlainTextToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
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
 */
class StrongToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

/**
 * Examples:
 * ```
 * *emphasis*
 * ```
 * ```
 * _emphasis_
 * ```
 */
class EmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}
