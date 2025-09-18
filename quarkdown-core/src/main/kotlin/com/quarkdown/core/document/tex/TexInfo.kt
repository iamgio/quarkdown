package com.quarkdown.core.document.tex

/**
 * Immutable TeX configuration that affects math typesetting.
 * @param macros custom user-defined macros to be used in math expressions
 */
data class TexInfo(
    val macros: Map<String, String> = emptyMap(),
)
