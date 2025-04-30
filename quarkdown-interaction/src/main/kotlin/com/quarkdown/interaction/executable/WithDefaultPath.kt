package com.quarkdown.interaction.executable

/**
 * Supplier of a default path of an executable.
 */
interface WithDefaultPath {
    /**
     * The default path to the executable, either statically defined or platform-dependent.
     */
    val defaultPath: String
}
