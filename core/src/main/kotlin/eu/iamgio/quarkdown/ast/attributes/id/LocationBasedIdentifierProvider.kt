package eu.iamgio.quarkdown.ast.attributes.id

import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.attributes.SectionLocation
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentInfo

/**
 *
 */
class LocationBasedIdentifierProvider(
    private val locations: Map<LocationTrackableNode, SectionLocation>,
    private val documentInfo: DocumentInfo,
) : IdentifierProvider<String> {
    // TODO make DocumentNumberingFormats class instead of DocumentInfo
    private val figureIds = mutableMapOf<ImageFigure, Int>()

    override fun visit(heading: Heading) = throw UnsupportedOperationException("Use a render-specific provider to generate heading IDs.")

    override fun visit(figure: ImageFigure): String {
        val figures = locations.keys.filterIsInstance<ImageFigure>() // Already sorted by location, ascending.
    }

    companion object {
        fun of(context: Context) = LocationBasedIdentifierProvider(context.attributes.locations, context.documentInfo)
    }
}
