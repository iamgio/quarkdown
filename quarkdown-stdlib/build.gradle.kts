plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.3.9"
}

dependencies {
    compileOnly(project(":quarkdown-native-library-processor"))
    ksp(project(":quarkdown-native-library-processor"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation(testFixtures(project(":quarkdown-core")))
    implementation(project(":quarkdown-core"))
    implementation("se.sawano.java:alphanumeric-comparator:2.0.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    implementation("org.kodein.emoji:emoji-kt:2.4.0")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}
