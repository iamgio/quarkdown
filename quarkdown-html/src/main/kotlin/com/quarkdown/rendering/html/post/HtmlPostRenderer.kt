package com.quarkdown.rendering.html.post

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.orDefault
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.rendering.html.post.resources.MediaPostRendererResource
import com.quarkdown.rendering.html.post.resources.PostRendererResource
import com.quarkdown.rendering.html.post.resources.ProxiedPostRendererResource
import com.quarkdown.rendering.html.post.resources.ScriptPostRendererResource
import com.quarkdown.rendering.html.post.resources.SearchIndexPostRendererResource
import com.quarkdown.rendering.html.post.resources.ThemePostRendererResource
import com.quarkdown.rendering.html.search.SearchIndexGenerator

// Default theme components to use if not specified by the user.
private val DEFAULT_THEME =
    DocumentTheme(
        color = "paperwhite",
        layout = "latex",
    )

/**
 * A [PostRenderer] that injects content into an HTML template. This includes all the features of [HtmlOnlyPostRenderer], plus:
 * - Theme components
 * - Runtime scripts
 * - Media resources
 *
 * @param context the [Context] of the document being rendered
 * @param relativePathToRoot relative path from the current document to the root document, used to correctly link resources
 * @param baseTemplateProcessor supplier of the base [TemplateProcessor] to inject with content and process via [HtmlPostRendererTemplate]
 * @param base the base [HtmlOnlyPostRenderer] to delegate HTML generation to
 * @param resourcesProvider supplier of the set of [PostRendererResource] to include in the output. Delegation to [base] is always included
 */
class HtmlPostRenderer(
    val context: Context,
    relativePathToRoot: String = ".",
    private val baseTemplateProcessor: () -> TemplateProcessor = baseHtmlTemplateProcessor,
    private val base: HtmlOnlyPostRenderer = HtmlOnlyPostRenderer(context, baseTemplateProcessor, relativePathToRoot = relativePathToRoot),
    private val resourcesProvider: () -> Set<PostRendererResource> =
        {
            setOf(
                ThemePostRendererResource(
                    theme = context.documentInfo.theme.orDefault(DEFAULT_THEME),
                    locale = context.documentInfo.locale,
                ),
                ScriptPostRendererResource(),
                MediaPostRendererResource(context.mediaStorage),
                SearchIndexPostRendererResource(SearchIndexGenerator.generate(context.subdocumentGraph)),
            )
        },
) : PostRenderer by base {
    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The HTML content is always included, regardless of the other options.
            val resources = resourcesProvider() + ProxiedPostRendererResource(base)
            resources.forEach { it.includeTo(this, rendered) }
        }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ) = OutputResourceGroup(name, resources)
}
