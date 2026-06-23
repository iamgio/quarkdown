package com.quarkdown.core.lexer

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.wrapIfPrimitive
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * A wrapper of a [TokenData] that may be parsed in order to extract information.
 * A token can be parsed into a [com.quarkdown.core.ast.Node].
 * @param data the wrapped token
 */
abstract class Token(
    val data: TokenData,
) {
    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit
     */
    abstract fun <T> accept(visitor: TokenVisitor<T>): T
}

/**
 * Lazily accepts a sequence of tokens to a shared visitor.
 * @param visitor the visitor to visit for each token.
 * @return a lazy sequence of results from each visit
 */
private fun <T> Sequence<Token>.acceptAll(visitor: TokenVisitor<T>): Sequence<T> = this.map { it.accept(visitor) }

/**
 * Like the generic [acceptAll], but specialized to [Node] output: each parsed node is also passed
 * through [wrapIfPrimitive], so primitive nodes (e.g. [com.quarkdown.core.ast.base.block.Heading])
 * are wrapped into a [com.quarkdown.core.ast.quarkdown.FunctionCallNode] backed by their stdlib
 * function, both at the top level (see [com.quarkdown.core.pipeline.stages.ParsingStage]) and inside
 * nested content (see [com.quarkdown.core.parser.BlockTokenParser]).
 *
 * @param visitor parser to visit each token with
 * @param context context the wrapped calls are registered under
 * @param isBlock whether the produced nodes belong in a block-level position; carried into the
 *                resulting wrap so the expander selects the right value→node mapper
 * @return a lazy sequence of parsed nodes, with primitives wrapped as function calls
 */
fun Sequence<Token>.acceptAll(
    visitor: TokenVisitor<Node>,
    context: MutableContext,
    isBlock: Boolean,
): Sequence<Node> = acceptAll(visitor).map { it.wrapIfPrimitive(context, isBlock) }
