package com.quarkdown.stdlib

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.stdlib.Stdlib.LOCALIZATION_TABLE

/**
 * Fallback value for non-existent elements in collections, dictionaries, and more.
 */
val NOT_FOUND: OutputValue<*>
    get() = NoneValue

/**
 * Exporter of Quarkdown's standard library.
 */
object Stdlib : LibraryExporter {
    /**
     * The name of the localization table used by this library.
     */
    private const val LOCALIZATION_TABLE = "std"

    override val library: Library
        get() =
            MultiFunctionLibraryLoader(name = "stdlib")
                .load(
                    Document +
                        Layout +
                        Text +
                        Math +
                        Logical +
                        String +
                        Collection +
                        Dictionary +
                        Optionality +
                        Logger +
                        Flow +
                        TableComputation +
                        Data +
                        Localization +
                        Library +
                        Slides +
                        Ecosystem +
                        Injection +
                        Mermaid,
                ).withHooks(
                    PipelineHooks(
                        // Localization data is loaded before any function is called.
                        afterRegisteringLibraries = {
                            includeResource(
                                this.readOnlyContext,
                                javaClass.getResourceAsStream("/lib/localization.qmd")!!.reader(),
                            )
                        },
                    ),
                )

    /**
     * Localizes a key from the stdlib table ([LOCALIZATION_TABLE]).
     * @param key localization key
     * @param context context to localize for
     * @return the localized string if the [key] exists in the `std` table, `null` otherwise
     */
    fun localizeOrNull(
        key: String,
        context: Context,
    ): String? = context.localizeOrNull(LOCALIZATION_TABLE, key.lowercase())
}
