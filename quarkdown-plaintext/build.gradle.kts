plugins {
    id("quarkdown.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":quarkdown-core"))
        }
    }
}
