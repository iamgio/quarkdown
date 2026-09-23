plugins {
    id("quarkdown.multiplatform")
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
        }
    }
}
