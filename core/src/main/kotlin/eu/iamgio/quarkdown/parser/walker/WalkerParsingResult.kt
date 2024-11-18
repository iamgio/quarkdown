package eu.iamgio.quarkdown.parser.walker

/**
 *
 */
data class WalkerParsingResult<T>(val value: T, val endIndex: Int, val remainder: CharSequence)
