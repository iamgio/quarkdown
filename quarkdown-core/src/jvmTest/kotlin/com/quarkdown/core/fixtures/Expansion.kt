@file:QModule

package com.quarkdown.core.fixtures

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture module backing FunctionNodeExpansionTest: functions called from a parsed Quarkdown source.
 */

@QFunction
fun expansionSum(
    a: Number,
    b: Number,
): NumberValue = NumberValue(a.toFloat() + b.toFloat())

@QFunction
@NotForDocumentType(DocumentType.SLIDES)
fun myFunction(x: String): StringValue = StringValue(x)

@QFunction
fun echoBoolean(value: Boolean): BooleanValue = BooleanValue(value)

@QFunction
fun expansionEchoEnum(value: NodeStyle.Alignment): StringValue = StringValue(value.name)

@QFunction
fun resourceContent(path: String): StringValue =
    StringValue(
        object {}::class.java
            .getResourceAsStream("/function/$path")!!
            .reader()
            .readText(),
    )

@QFunction
fun setAndEchoDocumentName(
    @Injected context: MutableContext,
    name: String,
): StringValue {
    context.documentInfo = context.documentInfo.copy(name = name)
    return StringValue(context.documentInfo.name!!)
}

@QFunction
fun makeQuote(body: MarkdownContent): NodeValue = NodeValue(BlockQuote(content = body.children))
