plugins {
    kotlin("jvm") version "1.8.21"
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
}
