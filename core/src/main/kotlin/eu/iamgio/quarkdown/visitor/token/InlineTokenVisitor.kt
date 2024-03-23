@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.visitor.token

import eu.iamgio.quarkdown.lexer.CodeSpanToken
import eu.iamgio.quarkdown.lexer.CommentToken
import eu.iamgio.quarkdown.lexer.CriticalContentToken
import eu.iamgio.quarkdown.lexer.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.EntityToken
import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.ImageToken
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.LinkToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.StrikethroughToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.UrlAutolinkToken

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
}
