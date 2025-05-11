extra["noRuntime"] = true

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation(kotlin("reflect"))

    val dokkaVersion = "2.0.0"
    val dokkaTestVersion = "1.9.20" // Dokka V2 has testing issues.

    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaTestVersion")
    testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaTestVersion")
    testImplementation("org.jsoup:jsoup:1.20.1")
}
