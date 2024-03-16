package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.log.DebugFormatter
import eu.iamgio.quarkdown.log.Log
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
    val lexer = flavor.lexerFactory.newInlineLexer(sourceFile.readText()) // debug
    val tokens = lexer.tokenize()

    Log.debug("Tokens:\n" + DebugFormatter.formatTokens(tokens))

    val parser = flavor.parserFactory.newParser()
    val document = Document(children = tokens.acceptAll(parser))

    Log.debug("AST:\n" + DebugFormatter.formatAST(document))
}
