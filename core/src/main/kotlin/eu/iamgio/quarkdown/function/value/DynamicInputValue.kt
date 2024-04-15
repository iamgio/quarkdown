package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.error.NoSuchElementFunctionException
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

/**
 * An [InputValue] whose type has not been yet determined.
 * @param unwrappedValue raw, unprocessed representation of the wrapped value
 */
data class DynamicInputValue(override val unwrappedValue: String) : InputValue<String> {
    /**
     * @param type type of the value to convert this automatic value to
     * @param call function call that contains this value, used to extract its context
     * @return a new typed [InputValue], automatically determined from [type], or `null` if it could not be converted
     */
    @Suppress("UNCHECKED_CAST")
    fun convertTo(
        type: KClass<out InputValue<*>>,
        call: FunctionCall<*>,
    ): InputValue<*>? {
        // Special treatment for enum values.
        if (type.isSubclassOf(Enum::class)) {
            // Enum.values() function lookup.
            val valuesFunction = type.functions.first { it.name == "values" } as KFunction<Array<Enum<*>>>
            val values = valuesFunction.call()

            return ValueFactory.enum(unwrappedValue, values)
                ?: throw NoSuchElementFunctionException(element = unwrappedValue, values)
        }

        // Gets ValueFactory methods annotated with @FromDynamicType(X::class),
        // and the one with a matching type is invoked.
        for (function in ValueFactory::class.declaredFunctions) {
            val from = function.findAnnotation<FromDynamicType>() ?: continue
            if (type.isSubclassOf(from.unwrappedType)) { // The factory method is suitable.
                return when {
                    // Fetch the context from the function call if it's required.
                    from.requiresContext -> {
                        val context =
                            call.context
                                ?: throw IllegalStateException("Call to ${call.function.name} does not have an attached context")
                        function.call(ValueFactory, unwrappedValue, context)
                    }

                    else -> function.call(ValueFactory, unwrappedValue)
                } as InputValue<*>?
            }
        }

        throw IllegalArgumentException("Cannot convert DynamicInputValue to type $type")
    }

    private fun getValueFactoryMethodResult(function: KFunction<*>) {
    }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * When a [ValueFactory] method is marked with this annotation, it is a candidate for type conversion from a [DynamicInputValue].
 * @param unwrappedType when an object matches this type, the function is suitable for invocation
 * @param requiresContext whether the factory method requires the [FunctionCall]'s context as an argument
 * @see ValueFactory
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FromDynamicType(val unwrappedType: KClass<*>, val requiresContext: Boolean = false)
