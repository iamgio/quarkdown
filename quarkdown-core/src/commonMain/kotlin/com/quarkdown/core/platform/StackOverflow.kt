package com.quarkdown.core.platform

/**
 * @return whether this throwable signals that the call stack overflowed,
 *         on platforms where such a failure can be observed at all
 */
internal expect fun Throwable.isStackOverflow(): Boolean
