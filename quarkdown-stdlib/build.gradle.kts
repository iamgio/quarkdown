plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    implementation("org.jetbrains.dokka:dokka-base:2.0.0")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}
