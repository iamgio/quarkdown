// Build-time only: ships with the JARs but isn't pulled into the runtime distribution.
extra["noRuntime"] = true

plugins {
    id("quarkdown.jvm")
    alias(libs.plugins.ksp)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation(project(":quarkdown-core"))
    kspTest(project(":quarkdown-native-library-processor"))
    implementation(libs.ksp.symbol.processing.api)
    implementation(project(":quarkdown-native-library-annotations"))
}

tasks.test {
    useJUnitPlatform()
}
