package com.quarkdown.quarkdoc.dokka.storage

import com.quarkdown.quarkdoc.dokka.util.sourcePaths
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.WithSources

data class StoredModule(
    val name: String,
    val dri: DRI,
)

/**
 * Storage that assigns Quarkdown modules to their .kt source file.
 */
object QuarkdownModulesStorage {
    // Path to the source file of the module declaration associated with its module
    private val modules = mutableMapOf<String, StoredModule>()

    operator fun get(sourcePath: String): StoredModule? = modules[sourcePath]

    operator fun set(
        sourcePath: String,
        module: StoredModule,
    ) {
        modules[sourcePath] = module
    }

    fun isModule(documentable: Documentable) =
        documentable is WithSources &&
            documentable.sourcePaths.any { it in modules }

    /**
     * The number of modules stored in this storage.
     */
    val moduleCount: Int
        get() = modules.size

    fun clear() {
        modules.clear()
    }
}
