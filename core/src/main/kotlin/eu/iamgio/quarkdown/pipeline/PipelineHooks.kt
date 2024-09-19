package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.lexer.Token

/**
 * Actions to run after each stage of a [Pipeline] has been completed.
 * @param afterRegisteringLibraries action to run after the libraries have been registered and are ready to be looked up (libraries as arguments)
 * @param afterLexing action to run after the tokens have been produced (output tokens as arguments)
 * @param afterParsing action to run after the AST has been generated (root as an argument)
 * @param beforeExpanding action to run before function calls are evaluated (main context as an argument).
 * The difference between this and [afterParsing] is that [afterParsing] might be called multiple times in case the parsing process is invoked internally,
 * for example when including external files from a function. [beforeExpanding] is called only once, between the main parsing process and the function calls expansion.
 * Also, at the time of this hook, [eu.iamgio.quarkdown.ast.AstAttributes.root] is set, while it is not at the time of [afterParsing]
 * @param afterExpanding action to run after the queued function calls have been expanded (root as an argument)
 * @param afterTreeVisiting action to run after the produced AST has been visited
 * @param afterRendering action to run after the rendered output code has been generated (output code as an argument)
 * @param afterPostRendering action to run after the rendered output code has been manipulated (e.g. wrapped) (output code as an argument)
 * @see Pipeline
 */
data class PipelineHooks(
    val afterRegisteringLibraries: Pipeline.(Set<Library>) -> Unit = {},
    val afterLexing: Pipeline.(List<Token>) -> Unit = {},
    val afterParsing: Pipeline.(AstRoot) -> Unit = {},
    val beforeExpanding: Pipeline.(Context) -> Unit = {},
    val afterExpanding: Pipeline.(AstRoot) -> Unit = {},
    val afterTreeVisiting: Pipeline.() -> Unit = {},
    val afterRendering: Pipeline.(CharSequence) -> Unit = {},
    val afterPostRendering: Pipeline.(CharSequence) -> Unit = {},
)
