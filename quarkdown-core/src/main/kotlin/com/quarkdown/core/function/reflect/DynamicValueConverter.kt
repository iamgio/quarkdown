package com.quarkdown.core.function.reflect

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.error.NoSuchElementException
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.InputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.factory.ValueFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotations
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
     * @param context context to evaluate the value for
     * @return a new typed [InputValue], automatically determined from [type], or `null` if it could not be converted
     * @throws IllegalArgumentException if the value could not be converted to the target type or if [context] is required and it's `null`
     * @throws NoSuchElementException if the value could not be converted to an enum entry
     */
    @Suppress("UNCHECKED_CAST")
    fun convertTo(
        type: KClass<*>,
        context: Context?,
    ): InputValue<*>? {
        val raw = value.unwrappedValue ?: return null

        // If the target type is dynamic, do nothing.
        // For instance, custom functions defined from a Quarkdown function have dynamic-type parameters.
        if (type.isSubclassOf(DynamicValue::class)) {
            return value
        }

        // Special treatment for enum values.
        if (type.isSubclassOf(Enum::class)) {
            // Enum.values() function lookup.
            val valuesFunction = type.functions.first { it.name == "values" } as KFunction<Array<Enum<*>>>
            val values = valuesFunction.call()

            return ValueFactory.enum(raw, values)
                ?: throw NoSuchElementException(element = raw, values)
        }

        // Gets ValueFactory methods annotated with @FromDynamicType(X::class),
        // and the one with a matching type is invoked.
        for (function in ValueFactory::class.declaredFunctions) {
            val annotations = function.findAnnotations<FromDynamicType>()
            val from = annotations.find { type.isSubclassOf(it.unwrappedType) } ?: continue

            // The factory method is suitable. Invoking it.

            return try {
                when {
                    // Fetch the context from the function call if it's required.
                    from.requiresContext -> {
                        if (context == null) {
                            throw IllegalStateException("Function call does not have an attached context")
                        }
                        function.call(ValueFactory, raw, context)
                    }

                    else -> function.call(ValueFactory, raw)
                } as InputValue<*>?
            } catch (e: InvocationTargetException) {
                throw e.cause ?: e
            }
        }

        throw IllegalArgumentException("Cannot convert DynamicValue to type $type")
    }
}

/**
 * When a [ValueFactory] method is marked with this annotation, it is a candidate for type conversion from a [DynamicValue].
 * @param unwrappedType when an object matches this type, the function is suitable for invocation
 * @param requiresContext whether the factory method requires the [FunctionCall]'s context as an argument
 * @see ValueFactory
 */
@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class FromDynamicType(val unwrappedType: KClass<*>, val requiresContext: Boolean = false)
