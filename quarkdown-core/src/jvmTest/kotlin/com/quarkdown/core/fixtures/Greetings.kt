@file:QModule

package com.quarkdown.core.fixtures

import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture module backing StandaloneFunctionTest.
 * Each function covers one binding or coercion behavior.
 */

@QFunction
fun greetNoArgs(): StringValue = StringValue("Hello")

@QFunction
fun greetWithArgs(
    to: String,
    from: String,
): StringValue = StringValue("Hello $to from $from")

@QFunction
fun greetWithOptionalArgs(
    to: String = "you",
    from: String = "me",
): StringValue = StringValue("Hello $to from $from")

@QFunction
fun greetWithOptionalArgsInTheMiddle(
    to: String = "you",
    from: String = "me",
    content: String,
): StringValue = StringValue("Hello $to from $from: $content")

@QFunction
fun greetWithExplicitBody(
    @Body content: String,
    to: String = "you",
    from: String = "me",
): StringValue = StringValue("Hello $to from $from: $content")

@QFunction
fun sum(
    a: Int,
    b: Int,
): NumberValue = NumberValue(a + b)

@QFunction
fun identity(x: Int): NumberValue = NumberValue(x)

@QFunction
fun echoEnum(value: NodeStyle.Alignment): StringValue = StringValue(value.name)

@QFunction
fun setDocumentName(
    @Injected context: MutableContext,
    name: String,
): VoidValue {
    context.documentInfo = context.documentInfo.copy(name = name)
    return VoidValue
}

@QFunction
@OnlyForDocumentType(DocumentType.SLIDES)
fun slidesOnlyGreet(): StringValue = StringValue("Hello")

@QFunction
@NotForDocumentType(DocumentType.SLIDES)
fun allButSlidesGreet(): StringValue = StringValue("Hello")
