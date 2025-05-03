package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.transformers.NameTransformer
import com.quarkdown.quarkdoc.dokka.transformers.RenamingsStorer
import com.quarkdown.quarkdoc.dokka.transformers.SuppressInjectedTransformer
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

/**
 * Dokka plugin that generates ad-hoc documentation for native Quarkdown functions.
 */
@Suppress("unused")
class QuarkdocDokkaPlugin : DokkaPlugin() {
    private val base by lazy { plugin<DokkaBase>() }

    /**
     * Functions and parameters annotated with `@Name` are renamed in the generated documentation.
     * This includes:
     * - Direct links (`[name]`)
     * - Parameter (`@param name`)
     * - See references (`@see name`)
     */
    val nameTransformer by extending {
        base.preMergeDocumentableTransformer providing ::NameTransformer
    }

    /**
     * Parameters annotated with `@Injected` are hidden (suppressed) in the generated documentation.
     */
    val suppressInjectedTransformer by extending {
        base.preMergeDocumentableTransformer providing ::SuppressInjectedTransformer
    }

    /**
     * Stores the old-new function name pairs, to be used in [nameTransformer].
     * This extension has to be last, so that it's executed first.
     */
    val renamingsStorer by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::RenamingsStorer
    }

    @DokkaPluginApiPreview
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}
