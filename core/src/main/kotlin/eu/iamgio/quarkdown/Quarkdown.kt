package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.rendering.RendererFactory
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Log.error("No source file passed.")
        exitProcess(NO_SOURCE_FILE_EXIT_CODE)
    }

    val flavor: MarkdownFlavor = QuarkdownFlavor

    val sourceFile = File(args.first())

    // val lexer = flavor.lexerFactory.newBlockLexer(sourceFile.readText())
    val lexer = flavor.lexerFactory.newInlineLexer(sourceFile.readText())
    val tokens = lexer.tokenize()

    if (Log.isDebug) {
        Log.debug("Tokens:\n" + DebugFormatter.formatTokens(tokens))
    }

    // Mutable attributes are affected by the parsing stage in order to store useful information.
    // This allows gathering information on-the-fly without additional visits of the whole tree.
    val attributes = MutableAstAttributes()
    val parser = flavor.parserFactory.newParser(attributes)
    val document = Document(children = tokens.acceptAll(parser))

    if (Log.isDebug) {
        Log.debug("AST:\n" + DebugFormatter.formatAST(document))
    }

    val renderer = RendererFactory.html(attributes)
    Log.info(renderer.visit(document))
}
