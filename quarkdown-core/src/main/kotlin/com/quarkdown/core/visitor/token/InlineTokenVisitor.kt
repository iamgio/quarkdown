package com.quarkdown.core.visitor.token

import com.quarkdown.core.lexer.tokens.CodeSpanToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.CriticalContentToken
import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.EntityToken
import com.quarkdown.core.lexer.tokens.EscapeToken
import com.quarkdown.core.lexer.tokens.ImageToken
import com.quarkdown.core.lexer.tokens.InlineMathToken
import com.quarkdown.core.lexer.tokens.LineBreakToken
import com.quarkdown.core.lexer.tokens.LinkToken
import com.quarkdown.core.lexer.tokens.PlainTextToken
import com.quarkdown.core.lexer.tokens.ReferenceFootnoteToken
import com.quarkdown.core.lexer.tokens.ReferenceImageToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken
import com.quarkdown.core.lexer.tokens.StrikethroughToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken
import com.quarkdown.core.lexer.tokens.TextSymbolToken
import com.quarkdown.core.lexer.tokens.UrlAutolinkToken

/**
 * A visitor for inline [com.quarkdown.core.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface InlineTokenVisitor<T> {
    fun visit(token: EscapeToken): T

    fun visit(token: EntityToken): T

    fun visit(token: CriticalContentToken): T

    fun visit(token: TextSymbolToken): T

    fun visit(token: CommentToken): T

    fun visit(token: LineBreakToken): T

    fun visit(token: LinkToken): T

    fun visit(token: ReferenceLinkToken): T

    fun visit(token: ReferenceFootnoteToken): T

    fun visit(token: DiamondAutolinkToken): T

    fun visit(token: UrlAutolinkToken): T

    fun visit(token: ImageToken): T

    fun visit(token: ReferenceImageToken): T

    fun visit(token: CodeSpanToken): T

    // Emphasis

    fun visit(token: PlainTextToken): T

    fun visit(token: EmphasisToken): T

    fun visit(token: StrongToken): T

    fun visit(token: StrongEmphasisToken): T

    fun visit(token: StrikethroughToken): T

    // Quarkdown extensions

    fun visit(token: InlineMathToken): T
}
