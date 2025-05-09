package com.quarkdown.quarkdoc.dokka.page

import com.quarkdown.core.function.value.quarkdownName
import com.quarkdown.quarkdoc.dokka.transformers.DocumentTarget
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.pages.ContentDivergentInstance
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.pages.WithDocumentables
import org.jetbrains.dokka.pages.recursiveMapTransform
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.transformers.pages.PageTransformer

private const val KDOC_TAG_HEADER_LEVEL = 4

/**
 * Given a function that is constrained to specific document types,
 * this page transformer explains these constraints in a new section of the documentation page.
 * @see com.quarkdown.quarkdoc.dokka.transformers.DocumentTypeConstraintsTransformer
 */
class DocumentTypeConstraintsPageTransformer(
    context: DokkaContext,
) : PageTransformer {
    private val builder: PageContentBuilder =
        PageContentBuilder(
            context.plugin<DokkaBase>().querySingle { commentsToContentConverter },
            context.plugin<DokkaBase>().querySingle { signatureProvider },
            context.logger,
        )

    override fun invoke(root: RootPageNode): RootPageNode {
        return root.transformContentPagesTree { page ->
            if (page !is WithDocumentables) return@transformContentPagesTree page

            val function =
                page.documentables.firstOrNull() as? DFunction
                    ?: return@transformContentPagesTree page

            val targets =
                function.extra[DocumentTarget]?.targets
                    ?: return@transformContentPagesTree page

            val contentBuilder =
                builder.DocumentableContentBuilder(
                    page.dri,
                    mainSourcesetData = function.sourceSets,
                    emptySet(),
                    PropertyContainer.empty(),
                )

            val newGroup =
                contentBuilder.buildGroup(styles = setOf(ContentStyle.KDocTag)) {
                    header(level = KDOC_TAG_HEADER_LEVEL, text = "Target")
                    text("This function is ")
                    text("only available", styles = setOf(TextStyle.Bold))
                    text(" for the following document types: ")
                    targets.forEachIndexed { index, target ->
                        codeInline { text(target.quarkdownName) }
                        text(if (index < targets.size - 1) ", " else ".")
                    }
                }

            val newContent =
                page.content.recursiveMapTransform<ContentDivergentInstance, ContentNode> { node ->
                    when (val after = node.after) {
                        is ContentGroup -> node.copy(after = after.copy(children = after.children + listOf(newGroup)))
                        null -> node.copy(after = newGroup)
                        else -> node
                    }
                }

            page.modified(content = newContent)
        }
    }
}
