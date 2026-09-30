package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.StrikethroughToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken

private const val STRONG_LENGTH = 2

/**
 * One piece in a doubly linked list, so a matched pair can be replaced by the node that holds their token.
 * @param piece the piece this node holds
 */
private class PieceNode(
    val piece: InlinePiece,
) {
    var previous: PieceNode? = null
    var next: PieceNode? = null
}

private val DelimiterRun.isStrikethrough: Boolean get() = char == STRIKETHROUGH_DELIMITER

/**
 * The key a closing run looks its `openersBottom` up by: its character, the residue of its original length
 * modulo three and whether it can also open, as CommonMark's "Process emphasis" prescribes.
 */
private val DelimiterRun.openersBottomKey: Int
    get() = (char.code * 3 + originalLength % 3) * 2 + if (canOpen) 1 else 0

/**
 * Resolves the pieces of an inline scan's first pass into tokens, pairing delimiter runs into nested
 * emphasis tokens per CommonMark 0.31.2 "Process emphasis" and turning everything left over into plain text.
 * @param source the padded source the pieces index into
 * @param fill constructor of the plain text token, or `null` to drop unclaimed spans
 */
internal class EmphasisResolver(
    private val source: CharSequence,
    private val fill: ((TokenData) -> Token)?,
) {
    private var head: PieceNode? = null

    /**
     * @param pieces the first pass's output, in source order
     * @return the tokens to emit, in source order
     */
    fun resolve(pieces: List<InlinePiece>): List<Token> {
        val nodes = pieces.map(::PieceNode)
        for (index in 1 until nodes.size) {
            nodes[index - 1].next = nodes[index]
            nodes[index].previous = nodes[index - 1]
        }
        head = nodes.firstOrNull()
        processEmphasis(nodes.filter { it.piece is InlinePiece.Delimiter })
        return flatten(head)
    }

    /**
     * CommonMark 0.31.2 "Process emphasis": walks the closers left to right, pairing each with the nearest
     * eligible opener before it.
     * @param delimiters every delimiter node, in source order
     */
    private fun processEmphasis(delimiters: List<PieceNode>) {
        val openersBottom = HashMap<Int, Int>()
        var closerIndex = 0
        while (closerIndex < delimiters.size) {
            val closerNode = delimiters[closerIndex]
            val closer = closerNode.run
            if (!closer.isActive || !closer.canClose) {
                closerIndex++
                continue
            }
            val key = closer.openersBottomKey
            val bottom = openersBottom[key] ?: -1
            var openerIndex = closerIndex - 1
            while (openerIndex > bottom && !delimiters[openerIndex].run.opens(closer)) openerIndex--
            if (openerIndex <= bottom) {
                openersBottom[key] = closerIndex - 1
                if (!closer.canOpen) closer.isActive = false
                closerIndex++
                continue
            }
            pair(delimiters[openerIndex], closerNode)
            for (between in openerIndex + 1 until closerIndex) delimiters[between].run.isActive = false
            if (closer.length == 0) closerIndex++
        }
    }

    /**
     * The rule of three: when either side can both open and close, the sum of the two original run lengths
     * may not be a multiple of three unless both lengths are. It does not apply to strikethrough.
     * @param closer the run that would close the pair
     * @return whether this run may open it
     */
    private fun DelimiterRun.opens(closer: DelimiterRun): Boolean {
        if (!isActive || !canOpen || char != closer.char) return false
        if (isStrikethrough) return true
        val bothWays = closer.canOpen || canClose
        val sumIsMultiple = (closer.originalLength + originalLength) % 3 == 0
        val bothAreMultiples = closer.originalLength % 3 == 0 && originalLength % 3 == 0
        return !(bothWays && sumIsMultiple && !bothAreMultiples)
    }

    /**
     * Consumes delimiters from both runs' inner ends and replaces the range between them with one token.
     * @param openerNode node holding the opening run
     * @param closerNode node holding the closing run
     */
    private fun pair(
        openerNode: PieceNode,
        closerNode: PieceNode,
    ) {
        val opener = openerNode.run
        val closer = closerNode.run
        val use = if (opener.isStrikethrough || (opener.length >= STRONG_LENGTH && closer.length >= STRONG_LENGTH)) 2 else 1
        val openDelimiterStart = opener.end - use
        val contentStart = opener.end
        val contentEnd = closer.start
        val closeDelimiterEnd = closer.start + use
        val children = detach(openerNode, closerNode)
        opener.length -= use
        closer.start += use
        closer.length -= use
        val data = TokenData(source.substring(openDelimiterStart, closeDelimiterEnd), openDelimiterStart until closeDelimiterEnd)
        val token = emphasisToken(data, use, opener.char, contentStart, contentEnd, children)
        val inserted = PieceNode(InlinePiece.Finished(token))
        inserted.previous = openerNode
        inserted.next = closerNode
        openerNode.next = inserted
        closerNode.previous = inserted
        if (opener.length == 0) {
            opener.isActive = false
            unlink(openerNode)
        }
        if (closer.length == 0) {
            closer.isActive = false
            unlink(closerNode)
        }
    }

    /**
     * @param data the token's data
     * @param use amount of delimiters the pair consumed from each side
     * @param char the delimiter character
     * @param contentStart index of the pair's content
     * @param contentEnd index just past the pair's content
     * @param children the resolved tokens between the delimiters
     * @return the token for the pair, collapsing an emphasis around a whole-content strong of the same
     *         character into a single strong emphasis, which is the node Quarkdown already renders
     */
    private fun emphasisToken(
        data: TokenData,
        use: Int,
        char: Char,
        contentStart: Int,
        contentEnd: Int,
        children: List<Token>,
    ): Token {
        if (char == STRIKETHROUGH_DELIMITER) return StrikethroughToken(data, children)
        if (use == STRONG_LENGTH) return StrongToken(data, children)
        val onlyChild = children.singleOrNull()
        val collapses =
            onlyChild is StrongToken &&
                onlyChild.data.position.first == contentStart &&
                onlyChild.data.position.last == contentEnd - 1 &&
                source[contentStart] == char
        return if (collapses) {
            StrongEmphasisToken(data, onlyChild.children)
        } else {
            EmphasisToken(data, children)
        }
    }

    /**
     * Removes every node strictly between two nodes and flattens them into the pair's children.
     * @param from node the range starts after
     * @param to node the range ends before
     * @return the children, in source order
     */
    private fun detach(
        from: PieceNode,
        to: PieceNode,
    ): List<Token> {
        val inner = mutableListOf<InlinePiece>()
        var cursor = from.next
        while (cursor != null && cursor !== to) {
            inner += cursor.piece
            cursor = cursor.next
        }
        from.next = to
        to.previous = from
        return flattenPieces(inner)
    }

    /**
     * Removes a node from the list.
     * @param node node to remove
     */
    private fun unlink(node: PieceNode) {
        if (node === head) head = node.next
        node.previous?.next = node.next
        node.next?.previous = node.previous
    }

    private val PieceNode.run: DelimiterRun get() = (piece as InlinePiece.Delimiter).run

    /**
     * @param head the list's first node, or `null` when it is empty
     * @return the tokens of the whole list
     */
    private fun flatten(head: PieceNode?): List<Token> {
        val pieces = mutableListOf<InlinePiece>()
        var cursor = head
        while (cursor != null) {
            pieces += cursor.piece
            cursor = cursor.next
        }
        return flattenPieces(pieces)
    }

    /**
     * Turns a run of pieces into tokens, coalescing adjacent unclaimed spans, and the residue of any
     * delimiter run that never paired, into single plain text tokens.
     * @param pieces pieces to flatten, in source order
     * @return the resulting tokens
     */
    private fun flattenPieces(pieces: List<InlinePiece>): List<Token> =
        buildList {
            var rawStart = -1
            var rawEnd = -1

            fun flushRaw() {
                if (rawStart < 0) return
                fill?.invoke(TokenData(source.substring(rawStart, rawEnd), rawStart until rawEnd))?.let { add(it) }
                rawStart = -1
            }

            fun addRaw(
                start: Int,
                end: Int,
            ) {
                if (start >= end) return
                if (rawStart < 0 || rawEnd != start) {
                    flushRaw()
                    rawStart = start
                }
                rawEnd = end
            }

            pieces.forEach { piece ->
                when (piece) {
                    is InlinePiece.Finished -> {
                        flushRaw()
                        add(piece.token)
                    }

                    is InlinePiece.Raw -> {
                        addRaw(piece.start, piece.end)
                    }

                    is InlinePiece.Delimiter -> {
                        addRaw(piece.run.start, piece.run.end)
                    }
                }
            }
            flushRaw()
        }
}
