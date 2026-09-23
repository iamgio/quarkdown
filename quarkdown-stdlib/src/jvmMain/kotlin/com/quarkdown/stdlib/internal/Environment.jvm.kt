package com.quarkdown.stdlib.internal

internal actual fun environmentVariable(name: String): String? = System.getenv(name)
