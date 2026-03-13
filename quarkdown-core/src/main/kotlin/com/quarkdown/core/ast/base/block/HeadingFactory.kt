package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull

/**
 * Creates an auto-generated [Heading] for a structural section of the document (e.g. table of contents, bibliography).
 *
 * The heading title is resolved from:
 * 1. A user-provided [title], if not `null` and not empty.
 * 2. A localized fallback from [localizationKey], if [title] is `null`.
 * 3. If neither resolves to text, the heading is still created with empty text when [customId] is set
 *    (to serve as a referenceable anchor), or omitted (`null`) otherwise.
 *
 * An explicitly empty [title] means no heading should be displayed at all.
 *
 * The resulting heading is marked with [Heading.excludeFromTableOfContents] to prevent self-referencing
 * in the document's table of contents.
 *
 * @param title user-provided title content.
 *              If `null`, the default localized title from [localizationKey] is used.
 *              If empty, no heading is created.
 * @param localizationKey key to look up the default localized title if [title] is `null`
 * @param context context for localization
 * @param depth depth of the heading (1-6)
 * @param customId optional custom ID for cross-referencing. If set and no title is resolved, the heading
 *                 is still created with empty text to act as an anchor
 * @param canBreakPage whether the heading can trigger an automatic page break
 * @param canTrackLocation whether the heading's position should be tracked and numbered.
 *                         Implicitly enabled when [includeInTableOfContents] is `true`.
 * @param includeInTableOfContents whether this heading should be indexed in the document's table of contents.
 *                                 Implicitly enables [canTrackLocation].
 * @return a [Heading] node, or `null` if [title] is explicitly empty
 *         or no title could be resolved and no [customId] is provided
 */
fun Heading.Companion.createSectionHeading(
    title: InlineContent?,
    localizationKey: String,
    context: Context,
    depth: Int = 1,
    customId: String? = null,
    canBreakPage: Boolean = true,
    canTrackLocation: Boolean = false,
    includeInTableOfContents: Boolean = false,
): Heading? {
    // An explicitly empty title means no heading should be shown.
    // null means "use default localized title", so null must not be treated as empty.
    if (title?.isEmpty() == true) {
        return null
    }

    val resolvedTitle =
        title
            ?: context.localizeOrNull(key = localizationKey)?.let { buildInline { text(it) } }
            ?: emptyList<Node>().takeIf { customId != null }
            ?: return null

    return Heading(
        depth = depth,
        text = resolvedTitle,
        customId = customId,
        canBreakPage = canBreakPage,
        canTrackLocation = canTrackLocation,
        excludeFromTableOfContents = !includeInTableOfContents,
    )
}
