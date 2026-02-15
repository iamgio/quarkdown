extra["noRuntime"] = true

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-quarkdoc-reader"))
    implementation(kotlin("reflect"))

    val dokkaVersion = "2.0.0"

    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")
    testRuntimeOnly("org.jetbrains.dokka:analysis-kotlin-symbols:$dokkaVersion")
    testImplementation("org.jsoup:jsoup:1.21.2")
}
