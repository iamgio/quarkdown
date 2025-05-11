package com.quarkdown.quarkdoc.dokka.signature

import com.quarkdown.quarkdoc.dokka.storage.QuarkdownModulesStorage
import com.quarkdown.quarkdoc.dokka.util.documentableContentBuilder
import org.jetbrains.dokka.base.signatures.KotlinSignatureProvider
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.pages.TokenStyle
import org.jetbrains.dokka.plugability.DokkaContext

/**
 *
 */
class QuarkdownSignatureProvider(
    private val context: DokkaContext,
) : SignatureProvider {
    private val kotlin = KotlinSignatureProvider(context)

    override fun signature(documentable: Documentable): List<ContentNode> {
        if (!QuarkdownModulesStorage.isModule(documentable)) {
            return kotlin.signature(documentable)
        }

        val builder =
            context.documentableContentBuilder(
                documentable,
                setOf(documentable.dri),
            )

        return builder
            .buildGroup {
                codeBlock {
                    text("test", styles = setOf(TokenStyle.Function))
                    text("hello", styles = setOf(TextStyle.Bold))
                }
            }.children
    }
}
