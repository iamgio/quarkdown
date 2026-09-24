package com.quarkdown.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/** Task generating `commonMain` sources when the source set is shared by several targets. */
const val KSP_COMMON_MAIN_TASK = "kspCommonMainKotlinMetadata"

/** Task generating `commonMain` sources on a single-target project, as part of the JVM compilation. */
const val KSP_JVM_TASK = "kspKotlinJvm"

private const val KSP_COMMON_MAIN_CONFIGURATION = "kspCommonMainMetadata"
private const val KSP_COMMON_MAIN_OUTPUT = "build/generated/ksp/metadata/commonMain/kotlin"
private const val KSP_JVM_CONFIGURATION = "kspJvm"
private const val KSP_JVM_OUTPUT = "generated/ksp/jvm/jvmMain/kotlin"

/**
 * Whether `commonMain` is shared by several targets, hence compiled on its own as metadata.
 * With a single target, `commonMain` is compiled together with that target's sources.
 */
val Project.hasSharedCommonMain: Boolean
    get() =
        extensions
            .getByType<KotlinMultiplatformExtension>()
            .targets
            .count { it.platformType != KotlinPlatformType.common } > 1

/**
 * Runs a KSP [processor] once over `commonMain` and makes its output part of `commonMain`,
 * so that every target compiles the same generated declarations.
 * Mirrors the wiring the amber plugin performs for its own processor.
 *
 * On a single-target project there is no metadata compilation to generate against,
 * so the processor runs as part of the JVM compilation instead, which compiles `commonMain` too.
 * @param processor dependency notation of the processor, e.g. a project dependency
 */
fun Project.kspCommonMain(processor: Any) {
    if (!hasSharedCommonMain) {
        kspCommonMainThroughJvm(processor)
        return
    }
    dependencies.add(KSP_COMMON_MAIN_CONFIGURATION, processor)
    extensions.getByType<KotlinMultiplatformExtension>().sourceSets.named("commonMain") {
        kotlin.srcDir(KSP_COMMON_MAIN_OUTPUT)
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        if (name != KSP_COMMON_MAIN_TASK) {
            dependsOn(KSP_COMMON_MAIN_TASK)
        }
    }
}

/**
 * Single-target wiring: the processor runs as part of the JVM compilation, which compiles `commonMain` too,
 * but its output is moved from `jvmMain` to `commonMain`, since common code cannot see declarations
 * that exist only in a platform source set.
 */
private fun Project.kspCommonMainThroughJvm(processor: Any) {
    dependencies.add(KSP_JVM_CONFIGURATION, processor)
    val output = layout.buildDirectory.dir(KSP_JVM_OUTPUT)
    val sourceSets = extensions.getByType<KotlinMultiplatformExtension>().sourceSets
    sourceSets.named("commonMain") {
        kotlin.srcDir(output)
    }
    afterEvaluate {
        sourceSets.named("jvmMain") {
            kotlin.setSrcDirs(kotlin.srcDirs.filterNot { it == output.get().asFile })
        }
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        dependsOn(tasks.matching { it.name == KSP_JVM_TASK })
    }
}
