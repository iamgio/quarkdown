package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.rendering.NodeRenderer
import eu.iamgio.quarkdown.visitor.token.TokenVisitor

/**
 * Strategies to use for each main stage of a [Pipeline].
 * @param lexer producer of tokens from a raw string source code
 * @param parser mapper of each token to a processed node of the Abstract Syntax Tree
 * @param libraryRegistrant component responsible for registering libraries, allowing look-ups
 * @param functionCallExpander producer of output content from function calls in the document
 * @param renderer mapper of each node of the tree to its string representation
 *                 in the language of the output target
 */
data class PipelineComponents(
    val lexer: Lexer,
    val parser: TokenVisitor<Node>,
    val libraryRegistrant: LibraryRegistrant,
    val functionCallExpander: FunctionCallNodeExpander,
    val renderer: NodeRenderer,
)
