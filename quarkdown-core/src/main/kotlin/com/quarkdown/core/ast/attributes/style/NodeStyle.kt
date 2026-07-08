package com.quarkdown.core.ast.attributes.style

import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * Style of a [StylableNode], such as a [com.quarkdown.core.ast.quarkdown.block.Container].
 * @param foregroundColor text color
 * @param backgroundColor background color
 * @param borderColor border color
 * @param borderWidth border width
 * @param borderStyle border style
 * @param margin whitespace outside the content
 * @param padding whitespace around the content
 * @param cornerRadius border radius of the container
 * @param alignment alignment of the content
 * @param textAlignment alignment of the text
 * @param textTransform transformation applied to the text content
 */
data class NodeStyle(
    val foregroundColor: Color? = null,
    val backgroundColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidth: Sizes? = null,
    val borderStyle: BorderStyle? = null,
    val margin: Sizes? = null,
    val padding: Sizes? = null,
    val cornerRadius: Sizes? = null,
    val alignment: Alignment? = null,
    val textAlignment: TextAlignment? = null,
    val textTransform: TextTransformData? = null,
) {
    /**
     * Possible alignment types.
     */
    enum class Alignment : RenderRepresentable {
        START,
        CENTER,
        END,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    /**
     * Possible text alignment types.
     * @param isLocal whether this alignment should be applied only to specific element types in the document,
     *                rather than globally to the entire document
     */
    enum class TextAlignment(
        val isLocal: Boolean = false,
    ) : RenderRepresentable {
        START,
        CENTER,
        END,
        JUSTIFY(isLocal = true),
        ;

        /**
         * Whether this alignment is applied globally to the document. This is complementary to [isLocal].
         * If true, it will be applied to all elements in the document.
         * If false, it will only be applied to specific elements that support this alignment.
         */
        val isGlobal: Boolean
            get() = !isLocal

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)

        companion object {
            /**
             * Converts an [Alignment] to a [TextAlignment], if applicable.
             */
            fun fromAlignment(alignment: Alignment): TextAlignment? =
                when (alignment) {
                    Alignment.START -> START
                    Alignment.CENTER -> CENTER
                    Alignment.END -> END
                }
        }
    }

    /**
     * Possible border types.
     */
    enum class BorderStyle : RenderRepresentable {
        /**
         * Solid border.
         */
        NORMAL,

        /**
         * Dashed border.
         */
        DASHED,

        /**
         * Dotted border.
         */
        DOTTED,

        /**
         * Double border.
         */
        DOUBLE,

        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
