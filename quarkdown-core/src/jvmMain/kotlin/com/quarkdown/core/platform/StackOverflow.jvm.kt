package com.quarkdown.core.platform

internal actual fun Throwable.isStackOverflow(): Boolean = this is StackOverflowError
