package com.quarkdown.core.log

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.lexer.Token

/**
 * Utilities to log prettier debugging data.
 */
object DebugFormatter {
    private fun String.replaceEscapeCharacters() =
        replace("\\R".toRegex(), "\\\\n")
            .replace("\t", "\\t")
            .replace("    ", "\\t")

    /**
     * Pretty-formats a list of tokens.
     * @param tokens tokens to format
     * @return formatted string
     */
    fun formatTokens(tokens: Iterable<Token>): String {
        val format = "%-25s %-20s %s" // Columns

        return tokens.joinToString(separator = "\n") { token ->
            val type = "type: ${token.javaClass.simpleName.removeSuffix("Token")}"
            val pos = "pos: ${token.data.position}"
            val content = token.data.text.replaceEscapeCharacters()

            val text = "text: $content"

            format.format(type, pos, text)
        }
    }

    /**
     * Pretty-formats an AST node.
     * @param node node to format
     * @return formatted string (without the 'children' attribute)
     */
    private fun formatNode(node: Node): String =
        buildString {
            // Remove children from the node's toString()
            val text = node.toString()
            val index = text.indexOf("children=")
            append(
                if (node is NestableNode && index >= 0) {
                    text.substring(0, index)
                } else {
                    text
                }.replaceEscapeCharacters(),
            )
            if (endsWith("(")) {
                setLength(length - 1)
            }
            if (endsWith(", ")) {
                setLength(length - 2)
                append(")")
            }
        }

    /**
     * Pretty-formats an AST.
     * @param root root node of the AST
     * @param initialIndent initial amount of indentation
     * @return formatted string
     */
    fun formatAST(
        root: NestableNode,
        initialIndent: Int = 0,
    ): String =
        buildString {
            val indent = "\t"
            append(indent.repeat(initialIndent))
            append(formatNode(root))
            append("\n")

            root.children.forEach {
                if (it is NestableNode) {
                    append(formatAST(it, initialIndent + 1))
                } else {
                    append(indent.repeat(initialIndent + 1)).append(formatNode(it))
                    append("\n")
                }
            }
        }

    /**
     * Pretty-formats a list of libraries.
     * @param libraries libraries to format
     * @return formatted string
     */
    fun formatLibraries(libraries: Iterable<Library>): String = libraries.joinToString { "${it.name} (${it.functions.size} functions)" }
}
