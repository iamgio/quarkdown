package eu.iamgio.quarkdown.common

import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.BlockTextToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken

/**
 * A visitor for block [eu.iamgio.quarkdown.lexer.TokenWrapper]s.
 * @param O output type of the `visit` methods
 */
interface BlockTokenVisitor<O> {
    fun visit(token: NewlineToken): O

    fun visit(token: BlockCodeToken): O

    fun visit(token: FencesCodeToken): O

    fun visit(token: HorizontalRuleToken): O

    fun visit(token: HeadingToken): O

    fun visit(token: SetextHeadingToken): O

    fun visit(token: LinkDefinitionToken): O

    fun visit(token: ListItemToken): O

    fun visit(token: HtmlToken): O

    fun visit(token: ParagraphToken): O

    fun visit(token: BlockQuoteToken): O

    fun visit(token: BlockTextToken): O
}
