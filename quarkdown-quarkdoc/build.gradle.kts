extra["noRuntime"] = true

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-quarkdoc-reader"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation(project(":quarkdown-native-library-processor"))
    implementation(kotlin("reflect"))

    val dokkaVersion = "2.2.0"

    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")
    testRuntimeOnly("org.jetbrains.dokka:analysis-kotlin-symbols:$dokkaVersion")
    implementation("com.fleeksoft.ksoup:ksoup:0.2.6")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
}
