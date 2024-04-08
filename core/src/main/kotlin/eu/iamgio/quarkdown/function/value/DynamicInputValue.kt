package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * An [InputValue] whose type has not been yet determined.
 * @param unwrappedValue raw, unprocessed representation of the wrapped value
 */
data class DynamicInputValue(override val unwrappedValue: String) : InputValue<String> {
    /**
     * @param type type of the value to convert this automatic value to
     * @return a new typed [InputValue], automatically determined from [type], or `null` if it could not be converted
     */
    fun convertTo(type: KClass<out InputValue<*>>): InputValue<*>? {
        // Gets ValueFactory methods annotated with @FromDynamicType(X::class),
        // and the one with a matching type is invoked.
        for (function in ValueFactory::class.declaredFunctions) {
            val from = function.findAnnotation<FromDynamicType>() ?: continue
            if (type.isSubclassOf(from.unwrappedType)) {
                return function.call(ValueFactory, unwrappedValue) as InputValue<*>?
            }
        }

        throw IllegalArgumentException("Cannot convert DynamicInputValue to type $type")
    }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * When a [ValueFactory] method is marked with this annotation, it is a candidate for type conversion from a [DynamicInputValue].
 * @param unwrappedType when an object matches this type, the function is suitable for invocation
 * @see ValueFactory
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FromDynamicType(val unwrappedType: KClass<*>)
