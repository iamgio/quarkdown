// Build-time only: ships with the JARs but isn't pulled into the runtime distribution.
extra["noRuntime"] = true

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.3.9"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation(project(":quarkdown-core"))
    kspTest(project(":quarkdown-native-library-processor"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.9")
}

tasks.test {
    useJUnitPlatform()
}
