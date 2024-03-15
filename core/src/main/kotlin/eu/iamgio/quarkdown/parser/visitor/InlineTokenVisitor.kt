package eu.iamgio.quarkdown.parser.visitor

import eu.iamgio.quarkdown.lexer.*

/**
 * A visitor for inline [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface InlineTokenVisitor<T> {
    fun visit(token: EscapeToken): T

    fun visit(token: CriticalCharacterToken): T

    fun visit(token: CommentToken): T

    fun visit(token: LineBreakToken): T

    fun visit(token: LinkToken): T

    fun visit(token: DiamondAutolinkToken): T

    fun visit(token: UrlAutolinkToken): T

    fun visit(token: ReferenceLinkToken): T

    fun visit(token: CollapsedReferenceLinkToken): T

    fun visit(token: ImageToken): T

    fun visit(token: ReferenceImageToken): T

    fun visit(token: CollapsedReferenceImageToken): T

    // Emphasis

    fun visit(token: PlainTextToken): T

    fun visit(token: EmphasisToken): T

    fun visit(token: StrongToken): T

    fun visit(token: StrongEmphasisToken): T
}
