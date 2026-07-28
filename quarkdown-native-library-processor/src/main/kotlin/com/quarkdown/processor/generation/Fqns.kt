package com.quarkdown.processor.generation

import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.SimpleFunction
import com.quarkdown.core.function.call.validate.DocumentTypeFunctionCallValidator
import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.NotForDocumentType
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.pipeline.error.PipelineException
import kotlin.reflect.KClass

/**
 * Fully-qualified names used by the code generator, derived at build time from real Kotlin
 * class references so they can't drift from the source of truth in `quarkdown-core`.
 *
 * Every string emitted into generated source that references a `quarkdown-core` symbol resolves
 * through this file, so a package rename or class move in core surfaces here as a compile error
 * instead of leaking through as a runtime `ClassNotFoundException` in generated code.
 */
internal object Fqns {
    val quarkdownModule: String = fqn(QuarkdownModule::class)
    val function: String = fqn(Function::class)
    val simpleFunction: String = fqn(SimpleFunction::class)
    val functionParameter: String = fqn(FunctionParameter::class)
    val dynamicValue: String = fqn(DynamicValue::class)
    val noneValue: String = fqn(NoneValue::class)
    val pipelineException: String = fqn(PipelineException::class)
    val functionCallRuntimeException: String = fqn(FunctionCallRuntimeException::class)
    val documentType: String = fqn(DocumentType::class)
    val documentTypeFunctionCallValidator: String = fqn(DocumentTypeFunctionCallValidator::class)

    /** FQN of the `@Injected` annotation, used to detect injected parameters at KSP time. */
    val injectedAnnotation: String = fqn(Injected::class)

    /** FQN of the `@Body` annotation, used to detect body-reserved parameters at KSP time. */
    val bodyAnnotation: String = fqn(Body::class)

    /** FQN of `@OnlyForDocumentType`, resolved into a compile-time document-type constraint. */
    val onlyForDocumentTypeAnnotation: String = fqn(OnlyForDocumentType::class)

    /** FQN of `@NotForDocumentType`, resolved into a compile-time document-type constraint. */
    val notForDocumentTypeAnnotation: String = fqn(NotForDocumentType::class)

    /**
     * FQN of the top-level `moduleOf(...)` function. There is no `KClass` for top-level
     * declarations, so we derive the package from a colocated class ([QuarkdownModule]) and
     * append the function's simple name — cheaper than defining a duplicate constant.
     */
    val moduleOf: String = packageOf(QuarkdownModule::class) + "." + ::moduleOf.name

    /**
     * All entries of [com.quarkdown.core.document.DocumentType] as declared in the enum,
     * derived at build time. Used to expand `@NotForDocumentType(...)` into the complementary
     * allowlist for the emitted `DocumentTypeFunctionCallValidator`.
     */
    val allDocumentTypeNames: List<String> = DocumentType.entries.map { it.name }

    private fun fqn(kClass: KClass<*>): String =
        kClass.qualifiedName
            ?: error("Class $kClass has no qualified name; cannot be referenced from generated code.")

    private fun packageOf(kClass: KClass<*>): String {
        val qn = fqn(kClass)
        val idx = qn.lastIndexOf('.')
        return if (idx < 0) "" else qn.substring(0, idx)
    }
}
