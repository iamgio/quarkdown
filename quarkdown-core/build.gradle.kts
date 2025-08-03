plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

dependencies {
    sequenceOf(kotlin("test"), "org.assertj:assertj-core:3.27.3").forEach {
        testFixturesImplementation(it)
        testImplementation(it)
    }
    testImplementation(testFixtures(project))
    implementation(kotlin("reflect"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("com.github.ajalt.colormath:colormath:3.6.1")
    implementation("com.github.fracpete:romannumerals4j:0.0.1")
    implementation("org.jbibtex:jbibtex:1.0.20")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
}
