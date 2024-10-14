package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.localization.localizeOrNull
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryExporter
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import kotlin.reflect.KFunction

/**
 * An exporter of a subsection of Quarkdown functions.
 */
typealias Module = Set<KFunction<OutputValue<*>>>

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
            MultiFunctionLibraryLoader(name = "stdlib").load(
                Document +
                    Layout +
                    Text +
                    Math +
                    Logical +
                    String +
                    Logger +
                    Flow +
                    Data +
                    Localization +
                    Library +
                    Slides +
                    Ecosystem,
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
