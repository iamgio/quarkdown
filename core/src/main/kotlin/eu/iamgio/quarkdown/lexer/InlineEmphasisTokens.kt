package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

// Inline emphasis helper tokens

class StrongEmphasisLeftDelimeterToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class StrongEmphasisRightDelimeterAsteriskToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class StrongEmphasisRightDelimeterUnderscoreToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

// ---

interface InlineSpan

interface InlineEmphasis : InlineSpan {
    val content: List<Token>
}

class InlineContentToken(data: TokenData) : Token(data), InlineSpan {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class StrongToken(data: TokenData, override val content: List<Token> = mutableListOf()) : Token(data), InlineEmphasis {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Strong$content"
    }
}

class EmphasisToken(data: TokenData, override val content: List<Token> = mutableListOf()) : Token(data), InlineEmphasis {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Emphasis$content"
    }
}

class StrongEmphasisToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class PlainTextToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>): T {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Plain(${data.text.trim()})"
    }
}
