package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.error.NoSuchElementFunctionException
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

/**
 * A [Value] whose type has not been yet determined.
 * - This is more commonly used as an [InputValue] to represent a value written by the user
 * that does not have a specific type yet.
 * - It is also used as an [OutputValue] by functions such as the stdlib `.function`, which
 * returns general content that can be used as any type, depending on the needs.
 * @param unwrappedValue raw, unprocessed representation of the wrapped value
 */
data class DynamicValue(override val unwrappedValue: String) : InputValue<String>, OutputValue<String> {
    /**
     * @param type target type to convert this dynamic value to.
     * This type is unwrapped (e.g. if [type] is `String`, the output is of type `StringValue`)
     * @param call function call that contains this value, used to extract its context
     * @return a new typed [InputValue], automatically determined from [type], or `null` if it could not be converted
     */
    @Suppress("UNCHECKED_CAST")
    fun convertTo(
        type: KClass<*>,
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

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * When a [ValueFactory] method is marked with this annotation, it is a candidate for type conversion from a [DynamicValue].
 * @param unwrappedType when an object matches this type, the function is suitable for invocation
 * @param requiresContext whether the factory method requires the [FunctionCall]'s context as an argument
 * @see ValueFactory
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FromDynamicType(val unwrappedType: KClass<*>, val requiresContext: Boolean = false)
