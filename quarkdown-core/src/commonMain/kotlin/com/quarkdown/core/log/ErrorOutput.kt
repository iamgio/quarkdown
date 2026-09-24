package com.quarkdown.core.log

/**
 * Prints [message] on the platform's error channel, followed by a line break.
 */
internal expect fun printErrorLine(message: String)
