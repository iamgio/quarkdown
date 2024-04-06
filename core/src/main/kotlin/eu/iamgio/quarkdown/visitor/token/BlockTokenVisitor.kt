package eu.iamgio.quarkdown.visitor.token

import eu.iamgio.quarkdown.lexer.tokens.BlockCodeToken
import eu.iamgio.quarkdown.lexer.tokens.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.tokens.BlockTextToken
import eu.iamgio.quarkdown.lexer.tokens.FencesCodeToken
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken
import eu.iamgio.quarkdown.lexer.tokens.HeadingToken
import eu.iamgio.quarkdown.lexer.tokens.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.tokens.HtmlToken
import eu.iamgio.quarkdown.lexer.tokens.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.tokens.ListItemToken
import eu.iamgio.quarkdown.lexer.tokens.MultilineMathToken
import eu.iamgio.quarkdown.lexer.tokens.NewlineToken
import eu.iamgio.quarkdown.lexer.tokens.OnelineMathToken
import eu.iamgio.quarkdown.lexer.tokens.OrderedListToken
import eu.iamgio.quarkdown.lexer.tokens.ParagraphToken
import eu.iamgio.quarkdown.lexer.tokens.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.tokens.TableToken
import eu.iamgio.quarkdown.lexer.tokens.UnorderedListToken

/**
 * A visitor for block [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface BlockTokenVisitor<T> {
    fun visit(token: NewlineToken): T

    fun visit(token: BlockCodeToken): T

    fun visit(token: FencesCodeToken): T

    fun visit(token: MultilineMathToken): T

    fun visit(token: OnelineMathToken): T

    fun visit(token: HorizontalRuleToken): T

    fun visit(token: HeadingToken): T

    fun visit(token: SetextHeadingToken): T

    fun visit(token: LinkDefinitionToken): T

    fun visit(token: UnorderedListToken): T

    fun visit(token: OrderedListToken): T

    fun visit(token: ListItemToken): T

    fun visit(token: TableToken): T

    fun visit(token: HtmlToken): T

    fun visit(token: ParagraphToken): T

    fun visit(token: BlockQuoteToken): T

    fun visit(token: BlockTextToken): T

    // Quarkdown extensions

    fun visit(token: FunctionCallToken): T
}
