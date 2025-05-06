package com.quarkdown.quarkdoc.dokka.storage

import org.jetbrains.dokka.links.DRI

data class StoredModule(
    val name: String,
    val dri: DRI,
)

/**
 *
 */
object QuarkdownModulesStorage {
    // Path to the source file of the module declaration associated with its module
    // private
    val modules = mutableMapOf<String, StoredModule>()

    operator fun get(sourcePath: String): StoredModule? = modules[sourcePath]

    operator fun set(
        sourcePath: String,
        module: StoredModule,
    ) {
        modules[sourcePath] = module
    }

    /**
     * The number of modules stored in this storage.
     */
    val moduleCount: Int
        get() = modules.size

    fun clear() {
        modules.clear()
    }
}
