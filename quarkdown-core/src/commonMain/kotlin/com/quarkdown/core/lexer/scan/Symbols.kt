package com.quarkdown.core.lexer.scan

/*
 * The syntax characters both the block pass and the inline pass read. Every other symbol is declared by the
 * recognizer or the helper that owns it.
 */

/**
 * Opens the bracketed label of a link, an image, a reference or a definition.
 */
internal const val LABEL_BEGIN = '['

/**
 * Closes a bracketed label.
 */
internal const val LABEL_END = ']'

/**
 * Separates a definition's label from its content.
 */
internal const val DEFINITION_MARKER = ':'

/**
 * Marks a footnote's label, right after [LABEL_BEGIN].
 */
internal const val FOOTNOTE_MARKER = '^'

/**
 * Delimits a fragment of code, alone or in a run of any length.
 */
internal const val BACKTICK = '`'

/**
 * Opens a destination enclosed in angle brackets, which may hold spaces.
 */
internal const val DESTINATION_BEGIN = '<'

/**
 * Closes a destination enclosed in angle brackets.
 */
internal const val DESTINATION_END = '>'
