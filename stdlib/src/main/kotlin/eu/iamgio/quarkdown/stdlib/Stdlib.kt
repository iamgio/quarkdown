package eu.iamgio.quarkdown.stdlib

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
    override val library: Library
        get() =
            MultiFunctionLibraryLoader(name = "stdlib").load(
                Document +
                    Layout +
                    Text +
                    Math +
                    Logical +
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
}
