package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle

private fun DokkaContext.contentBuilder() =
    PageContentBuilder(
        plugin<DokkaBase>().querySingle { commentsToContentConverter },
        plugin<DokkaBase>().querySingle { signatureProvider },
        logger,
    )

/**
 * Creates a [PageContentBuilder.DocumentableContentBuilder] for the given [Documentable].
 */
fun DokkaContext.documentableContentBuilder(
    documentable: Documentable,
    dri: Set<DRI>,
) = contentBuilder().DocumentableContentBuilder(
    dri,
    mainSourcesetData = documentable.sourceSets,
    emptySet(),
    PropertyContainer.empty(),
)
