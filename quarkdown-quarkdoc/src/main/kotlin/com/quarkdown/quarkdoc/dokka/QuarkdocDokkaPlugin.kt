package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.transformers.NameTransformer
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
    /**
     * Functions and parameters annotated with `@Name` are renamed in the generated documentation.
     */
    val nameTransformer by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::NameTransformer
    }

    /**
     * Parameters annotated with `@Injected` are hidden (suppressed) in the generated documentation.
     */
    val suppressInjectedTransformer by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::SuppressInjectedTransformer
    }

    @DokkaPluginApiPreview
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}
