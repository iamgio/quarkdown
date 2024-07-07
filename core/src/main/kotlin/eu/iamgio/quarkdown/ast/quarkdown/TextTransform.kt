package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Text transformation a portion of text can undergo.
 * If a property is set to `null` it is not specified, hence ignored.
 * @param size font size
 * @param weight font weight
 * @param style font style
 * @param decoration text decoration
 * @param case text case
 * @param variant font variant
 */
class TextTransformData(
    val size: Size? = null,
    val weight: Weight? = null,
    val style: Style? = null,
    val decoration: Decoration? = null,
    val case: Case? = null,
    val variant: Variant? = null,
) {
    enum class Size : RenderRepresentable {
        TINY,
        SMALL,
        NORMAL,
        MEDIUM,
        LARGE,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    enum class Weight : RenderRepresentable {
        NORMAL,
        BOLD,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    enum class Style : RenderRepresentable {
        NORMAL,
        ITALIC,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    enum class Decoration : RenderRepresentable {
        NONE,
        UNDERLINE,
        STRIKETHROUGH,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    enum class Case : RenderRepresentable {
        NONE,
        UPPERCASE,
        LOWERCASE,
        CAPITALIZE,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    enum class Variant : RenderRepresentable {
        NORMAL,
        SMALL_CAPS,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}

/**
 * A portion of text with a specific visual transformation.
 * @param data transformation the text undergoes
 */
data class TextTransform(
    val data: TextTransformData,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
