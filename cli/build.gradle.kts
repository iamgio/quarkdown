plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
    implementation(project(":stdlib"))
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}
