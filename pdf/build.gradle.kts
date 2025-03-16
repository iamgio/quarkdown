plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation(project(":core"))
    implementation(project(":server"))
}
