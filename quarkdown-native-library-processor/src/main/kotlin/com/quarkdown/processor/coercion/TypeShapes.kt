package com.quarkdown.processor.coercion

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * Projects a resolved KSP type down to the [TypeShape] the [CoercionPlanner] consumes.
 */
fun KSType.toTypeShape(): TypeShape {
    val classDeclaration = this.declaration as? KSClassDeclaration
    return TypeShape(
        qualifiedName = this.declaration.qualifiedName?.asString() ?: this.declaration.simpleName.asString(),
        simpleName = this.declaration.simpleName.asString(),
        supertypeQualifiedNames =
            classDeclaration
                ?.getAllSuperTypes()
                ?.mapNotNull { it.declaration.qualifiedName?.asString() }
                ?.toSet()
                .orEmpty(),
        isEnum = classDeclaration?.classKind == ClassKind.ENUM_CLASS,
        isNullable = this.isMarkedNullable,
    )
}
