plugins {
    kotlin("jvm") version "1.8.21"
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
}
