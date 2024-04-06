package eu.iamgio.quarkdown.visitor.token

import eu.iamgio.quarkdown.lexer.tokens.CodeSpanToken
import eu.iamgio.quarkdown.lexer.tokens.CommentToken
import eu.iamgio.quarkdown.lexer.tokens.CriticalContentToken
import eu.iamgio.quarkdown.lexer.tokens.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.tokens.EmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.EntityToken
import eu.iamgio.quarkdown.lexer.tokens.EscapeToken
import eu.iamgio.quarkdown.lexer.tokens.ImageToken
import eu.iamgio.quarkdown.lexer.tokens.InlineMathToken
import eu.iamgio.quarkdown.lexer.tokens.LineBreakToken
import eu.iamgio.quarkdown.lexer.tokens.LinkToken
import eu.iamgio.quarkdown.lexer.tokens.PlainTextToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.tokens.StrikethroughToken
import eu.iamgio.quarkdown.lexer.tokens.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.StrongToken
import eu.iamgio.quarkdown.lexer.tokens.UrlAutolinkToken

/**
 * A visitor for inline [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface InlineTokenVisitor<T> {
    fun visit(token: EscapeToken): T

    fun visit(token: EntityToken): T

    fun visit(token: CriticalContentToken): T

    fun visit(token: CommentToken): T

    fun visit(token: LineBreakToken): T

    fun visit(token: LinkToken): T

    fun visit(token: ReferenceLinkToken): T

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
