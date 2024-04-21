package eu.iamgio.quarkdown.function.reflect

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.error.NoSuchElementFunctionException
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.InputValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf

/**
 * A converter of a value that potentially holds any type (its value is stored as a plain string)
 * to a specific, statically defined [Value] type that can be used as an input for a function call argument.
 * @param value the dynamic value to convert to a typed value
 */
class DynamicValueConverter(private val value: DynamicValue) {
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
        val raw = value.unwrappedValue

        // Special treatment for enum values.
        if (type.isSubclassOf(Enum::class)) {
            // Enum.values() function lookup.
            val valuesFunction = type.functions.first { it.name == "values" } as KFunction<Array<Enum<*>>>
            val values = valuesFunction.call()

            return ValueFactory.enum(raw, values)
                ?: throw NoSuchElementFunctionException(element = raw, values)
        }

        // Gets ValueFactory methods annotated with @FromDynamicType(X::class),
        // and the one with a matching type is invoked.
        for (function in ValueFactory::class.declaredFunctions) {
            val from = function.findAnnotation<FromDynamicType>() ?: continue
            if (!type.isSubclassOf(from.unwrappedType)) continue

            // The factory method is suitable. Invoking it.

            return when {
                // Fetch the context from the function call if it's required.
                from.requiresContext -> {
                    val context =
                        call.context
                            ?: throw IllegalStateException("Call to ${call.function.name} does not have an attached context")
                    function.call(ValueFactory, raw, context)
                }

                else -> function.call(ValueFactory, raw)
            } as InputValue<*>?
        }

        throw IllegalArgumentException("Cannot convert DynamicInputValue to type $type")
    }
}

/**
 * When a [ValueFactory] method is marked with this annotation, it is a candidate for type conversion from a [DynamicValue].
 * @param unwrappedType when an object matches this type, the function is suitable for invocation
 * @param requiresContext whether the factory method requires the [FunctionCall]'s context as an argument
 * @see ValueFactory
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FromDynamicType(val unwrappedType: KClass<*>, val requiresContext: Boolean = false)
