plugins {
    id("quarkdown.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":quarkdown-core"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktoml.core)
        }
    }
}
