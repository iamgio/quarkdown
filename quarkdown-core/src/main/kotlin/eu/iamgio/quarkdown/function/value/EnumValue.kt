package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class EnumValue(override val unwrappedValue: Enum<*>) : InputValue<Enum<*>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * @return [this] enum wrapped into an [EnumValue]
 */
fun Enum<*>.wrappedAsValue() = EnumValue(this)

/**
 * @return this enum's name in Quarkdown format (lowercase, no underscores). i.e. `SPACE_AROUND` -> `spacearound`
 */
val Enum<*>.quarkdownName: String
    get() = name.lowercase().replace("_", "")
