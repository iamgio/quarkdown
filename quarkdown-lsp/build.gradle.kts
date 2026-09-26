plugins {
    id("quarkdown.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":quarkdown-core"))
            implementation(project(":quarkdown-quarkdoc-reader"))
        }
        jvmMain.dependencies {
            implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")
        }
        jvmTest.dependencies {
            implementation("io.mockk:mockk:1.14.11")
        }
    }
}
