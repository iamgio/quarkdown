package eu.iamgio.quarkdown.document.tex

/**
 * Mutable TeX configuration that affects math typesetting.
 * @param macros custom user-defined macros
 */
data class TexInfo(
    val macros: MutableMap<String, String> = mutableMapOf(),
)
