package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.transformers.NameTransformer
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

/**
 * Dokka plugin that generates ad-hoc documentation for native Quarkdown functions.
 *
 * Currently supported features:
 * - Functions and parameters annotated with `@Name` are renamed in the generated documentation.
 */
@Suppress("unused")
class QuarkdocDokkaPlugin : DokkaPlugin() {
    val nameTransformer by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::NameTransformer
    }

    @DokkaPluginApiPreview
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}
