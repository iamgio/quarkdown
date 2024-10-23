package eu.iamgio.quarkdown.ast.quarkdown.block.toc

import eu.iamgio.quarkdown.ast.base.block.FocusListItemVariant
import eu.iamgio.quarkdown.ast.base.block.ListItem
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.context.toc.TableOfContents

// Converts TOC items to a OrderedList.

/**
 * Converts a table of contents to a renderable [OrderedList].
 * @param items ToC items [this] view should contain
 * @param linkUrlMapper function that obtains the URL to send to when a ToC item is interacted with
 */
fun TableOfContentsView.convertToListNode(
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
                        item.text,
                        url = linkUrlMapper(item),
                        title = null,
                    )

                // Recursively include sub-items.
                item.subItems.filter { it.depth <= view.maxDepth }
                    .takeIf { it.isNotEmpty() }
                    ?.let { this += convertToListNode(it, linkUrlMapper) }
            }

        return OrderedList(
            startIndex = 1,
            isLoose = true,
            children =
                items.map {
                    ListItem(
                        // When at least one item is focused, the other items are less visible.
                        variants = listOf(FocusListItemVariant(isFocused = view.hasFocus(it))),
                        children = getTableOfContentsItemContent(it),
                    )
                },
        )
    }
