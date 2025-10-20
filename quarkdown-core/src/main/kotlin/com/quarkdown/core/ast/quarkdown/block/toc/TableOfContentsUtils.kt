package com.quarkdown.core.ast.quarkdown.block.toc

import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.quarkdown.block.list.FocusListItemVariant
import com.quarkdown.core.ast.quarkdown.block.list.LocationTargetListItemVariant
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.util.stripRichContent
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Converts a table of contents to a renderable [OrderedList].
 * @param renderer renderer to use to render items
 * @param items ToC items [this] view should contain
 * @param linkUrlMapper function that obtains the URL to send to when a ToC item is interacted with
 */
fun TableOfContentsView.convertToListNode(
    renderer: NodeVisitor<CharSequence>,
    items: List<TableOfContents.Item>,
    linkUrlMapper: (TableOfContents.Item) -> String,
): OrderedList =
    let { view ->
        // Gets the content of an inner TOC item.
        fun getTableOfContentsItemContent(item: TableOfContents.Item) =
            buildList {
                // A link to the target heading.
                this +=
                    Link(
                        // Rich content is ignored.
                        item.text.stripRichContent(renderer),
                        url = linkUrlMapper(item),
                        title = null,
                    )

                // Recursively include sub-items.
                item.subItems
                    .filter { it.depth <= view.maxDepth }
                    .let { subItems ->
                        // Filter unnumbered headings if needed.
                        if (!view.includeUnnumbered) {
                            subItems.filter { (it.target as? Heading)?.canTrackLocation != false }
                        } else {
                            subItems
                        }
                    }.takeIf { it.isNotEmpty() }
                    ?.let { this += convertToListNode(renderer, it, linkUrlMapper) }
            }

        return OrderedList(
            startIndex = 1,
            isLoose = true,
            children =
                items.map {
                    ListItem(
                        variants =
                            buildList {
                                // When at least one item is focused, the other items are less visible.
                                this += FocusListItemVariant(isFocused = view.hasFocus(it))

                                // If the target node's location can be tracked,
                                // the list item displays its location.
                                // Since the targets are usually headings, thus location trackable, this is applied.
                                // Only add location variant for numbered headings.
                                if (it.target is LocationTrackableNode && it.target.canTrackLocation) {
                                    this += LocationTargetListItemVariant(it.target, DocumentNumbering::headings)
                                }
                            },
                        children = getTableOfContentsItemContent(it),
                    )
                },
        )
    }
