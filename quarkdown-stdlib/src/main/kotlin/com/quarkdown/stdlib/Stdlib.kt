package com.quarkdown.stdlib

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.pipeline.PipelineHooks

/**
 * Fallback value for non-existent elements in collections, dictionaries, and more.
 */
val NOT_FOUND: OutputValue<*>
    get() = NoneValue

/**
 * Exporter of Quarkdown's standard library.
 */
object Stdlib : LibraryExporter {
    override val library: Library
        get() =
            MultiFunctionLibraryLoader(name = "stdlib")
                .load(
                    Document.Module,
                    Layout.Module,
                    Text.Module,
                    Primitives.Module,
                    MiscElements.Module,
                    Math.Module,
                    Logical.Module,
                    Strings.Module,
                    Icon.Module,
                    Emoji.Module,
                    Collection.Module,
                    Dictionary.Module,
                    Optionality.Module,
                    Logger.Module,
                    Flow.Module,
                    TableComputation.Module,
                    Data.Module,
                    Localization.Module,
                    Libraries.Module,
                    Slides.Module,
                    Ecosystem.Module,
                    Html.Module,
                    Mermaid.Module,
                    Reference.Module,
                    Bibliography.Module,
                    Process.Module,
                ).withHooks(
                    PipelineHooks(
                        // Localization data is loaded before any function is called.
                        afterRegisteringLibraries = {
                            includeResource(
                                this.readOnlyContext,
                                javaClass.getResourceAsStream("/lib/localization.qd")!!.reader(),
                            )
                        },
                    ),
                )
}
