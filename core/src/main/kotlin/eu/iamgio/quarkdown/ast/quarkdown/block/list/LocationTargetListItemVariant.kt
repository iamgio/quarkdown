package eu.iamgio.quarkdown.ast.quarkdown.block.list

import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.base.block.list.ListItem
import eu.iamgio.quarkdown.ast.base.block.list.ListItemVariant
import eu.iamgio.quarkdown.ast.base.block.list.ListItemVariantVisitor

/**
 * Variant of a [ListItem] that displays the location of a target node,
 * usually (rendering-dependent) by replacing the item marker with [target]'s position,
 * formatted according to the global numbering format.
 * This is used, for example, in table of contents.
 * @param target node to display the location of
 */
data class LocationTargetListItemVariant(val target: LocationTrackableNode) : ListItemVariant {
    override fun <T> accept(visitor: ListItemVariantVisitor<T>): T = visitor.visit(this)
}