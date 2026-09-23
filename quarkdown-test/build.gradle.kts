extra["noRuntime"] = true

plugins {
    id("quarkdown.jvm")
}

dependencies {
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(project(":quarkdown-install-layout-navigator"))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-html"))
    implementation(project(":quarkdown-markdown"))
    implementation(project(":quarkdown-plaintext"))
    implementation(project(":quarkdown-stdlib"))
}

tasks.test {
    useJUnitPlatform()
    // Resolve the synthetic `lib/` directory the same way the CLI does at runtime.
    dependsOn(":assembleDevLib")
}
