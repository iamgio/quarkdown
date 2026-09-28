package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.TrailingCustomId
import com.quarkdown.core.lexer.scan.scanner
import com.quarkdown.core.lexer.scan.takeCustomId

/**
 * What follows a math expression on its line may only be spaces, tabs and one optional custom ID.
 * @param from index the trailing part starts at
 * @return the parsed suffix, or `null` when the rest of the line holds anything else
 */
internal fun Line.trailingCustomIdOnly(from: Int): TrailingCustomId? =
    scanner(from = from).run {
        takeSpacesAndTabs()
        val value = takeCustomId()
        takeSpacesAndTabs()
        if (isAtEnd) TrailingCustomId(contentEnd = from, value = value) else null
    }
