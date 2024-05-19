package eu.iamgio.quarkdown.function.value.data

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
import eu.iamgio.quarkdown.function.reflect.FromDynamicType
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.ValueFactory

/**
 * An action block with a variable parameter count.
 * The return type is dynamic (a snippet of raw Quarkdown code is returned), hence it is evaluated and converted to a static type.
 * @param action action to perform, which takes a variable sequence of [Value]s as arguments and returns a Quarkdown code snippet.
 */
class Lambda(val action: (Array<out Value<*>>) -> String) {
    /**
     * Invokes the lambda action with given arguments.
     * @param context context this lambda lies in
     * @param values arguments of the lambda action
     * @param T **unwrapped** type to convert the resulting dynamic value to.
     * This type must appear in a [FromDynamicType] annotation on a [ValueFactory] method
     * @param V **wrapped** value type (which wraps [T]) to convert the resulting dynamic value to
     * @return the result of the lambda action, as a statically typed value
     */
    inline fun <reified T, reified V : Value<T>> invoke(
        context: Context,
        vararg values: Value<*>,
    ): V {
        val result = ValueFactory.expression(action(values), context)?.eval()

        return if (result is DynamicValue) {
            DynamicValueConverter(result).convertTo(T::class, context)
        } else {
            result
        } as? V
            ?: throw IllegalArgumentException("Unexpected lambda result: expected ${V::class}, found ${result?.let { it::class }}")
    }
}
