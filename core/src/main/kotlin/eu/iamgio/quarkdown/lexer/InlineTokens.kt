package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

// Inline tokens

class EscapeToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class InlineCodeToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class LineBreakToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class PlainTextToken(data: TokenData) : Token(data) {
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

class AutolinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class LinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class ReferenceLinkToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

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

class CommentToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

// Emphasis

class StrongToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class EmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}
