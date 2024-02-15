package eu.iamgio.quarkdown.ast

/**
 * An AST member.
 */
interface Node

/**
 * A node that may contain nested tokens.
 */
interface NestableNode : Node {
    val children: List<Node>
}
