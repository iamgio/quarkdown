package com.quarkdown.core.visitor.token

import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.BlockTextToken
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.lexer.tokens.HeadingToken
import com.quarkdown.core.lexer.tokens.HorizontalRuleToken
import com.quarkdown.core.lexer.tokens.HtmlToken
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken
import com.quarkdown.core.lexer.tokens.ListItemToken
import com.quarkdown.core.lexer.tokens.MultilineMathToken
import com.quarkdown.core.lexer.tokens.NewlineToken
import com.quarkdown.core.lexer.tokens.OnelineMathToken
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.PageBreakToken
import com.quarkdown.core.lexer.tokens.ParagraphToken
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import com.quarkdown.core.lexer.tokens.TableToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken

/**
 * A visitor for block [com.quarkdown.core.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface BlockTokenVisitor<T> {
    fun visit(token: NewlineToken): T

    fun visit(token: BlockCodeToken): T

    fun visit(token: FencesCodeToken): T

    fun visit(token: HorizontalRuleToken): T

    fun visit(token: HeadingToken): T

    fun visit(token: SetextHeadingToken): T

    fun visit(token: LinkDefinitionToken): T

    fun visit(token: FootnoteDefinitionToken): T

    fun visit(token: UnorderedListToken): T

    fun visit(token: OrderedListToken): T

    fun visit(token: ListItemToken): T

    fun visit(token: TableToken): T

    fun visit(token: HtmlToken): T

    fun visit(token: ParagraphToken): T

    fun visit(token: BlockQuoteToken): T

    fun visit(token: BlockTextToken): T

    // Quarkdown extensions

    fun visit(token: PageBreakToken): T

    fun visit(token: MultilineMathToken): T

    fun visit(token: OnelineMathToken): T

    fun visit(token: FunctionCallToken): T
}
