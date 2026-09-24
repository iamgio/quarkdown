import com.quarkdown.buildlogic.KSP_COMMON_MAIN_TASK
import com.quarkdown.buildlogic.KSP_JVM_TASK
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

if (tasks.findByName("test") == null) {
    tasks.register("test") {
        group = "verification"
        description = "Runs the tests of every target."
        dependsOn("allTests")
    }
}

// Sources generated into commonMain are registered as a plain source directory.
val commonMainGeneratorNames = setOf(KSP_COMMON_MAIN_TASK, KSP_JVM_TASK)
val commonMainGenerators = tasks.matching { it.name in commonMainGeneratorNames }!!
tasks
    .matching { (it.name.startsWith("runKtlint") || it.name.startsWith("ksp")) && it.name !in commonMainGeneratorNames }
    .configureEach { dependsOn(commonMainGenerators) }
tasks.matching { it.name == KSP_JVM_TASK }.configureEach {
    dependsOn(tasks.matching { it.name == KSP_COMMON_MAIN_TASK })
}

// Single-target projects have no metadata compilation: a no-op stand-in keeps those dependencies resolvable.
gradle.projectsEvaluated {
    if (tasks.findByName(KSP_COMMON_MAIN_TASK) == null) {
        tasks.register(KSP_COMMON_MAIN_TASK) {
            description = "Stand-in for the commonMain metadata generation, absent on single-target projects."
        }
    }
}
