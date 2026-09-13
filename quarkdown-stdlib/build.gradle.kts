plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.3.12"
}

dependencies {
    compileOnly(project(":quarkdown-native-library-processor"))
    ksp(project(":quarkdown-native-library-processor"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation(testFixtures(project(":quarkdown-core")))
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-install-layout-navigator"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("com.jsoizo:kotlin-csv:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
    implementation("org.kodein.emoji:emoji-kt:2.5.0")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}
