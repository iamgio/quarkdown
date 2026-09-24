import com.quarkdown.buildlogic.kspCommonMain

plugins {
    id("quarkdown.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":quarkdown-core"))
            implementation(project(":quarkdown-native-library-annotations"))
            implementation(libs.kotlinx.serialization.json)
            implementation("org.kodein.emoji:emoji-kt:2.5.0")
        }
        jvmMain.dependencies {
            implementation("com.jsoizo:kotlin-csv:2.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
        }
        jvmTest.dependencies {
            implementation("org.assertj:assertj-core:3.27.7")
            implementation(project(":quarkdown-core-test-fixtures"))
        }
    }
}

kspCommonMain(project(":quarkdown-native-library-processor"))

dependencies {
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}
