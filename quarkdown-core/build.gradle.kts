plugins {
    kotlin("jvm")
    id("com.quarkdown.amber") version "2.2.0"
    `java-test-fixtures`
}

dependencies {
    sequenceOf(kotlin("test"), "org.assertj:assertj-core:3.27.6").forEach {
        testFixturesImplementation(it)
        testImplementation(it)
    }
    testImplementation(testFixtures(project(path)))
    testCompileOnly(project(":quarkdown-native-library-processor"))
    kspTest(project(":quarkdown-native-library-processor"))
    ksp(project(":quarkdown-locale-table-processor"))
    api(libs.ktor.http)
    implementation("com.squareup.okio:okio:3.18.2")
    implementation("com.squareup.okio:okio-fakefilesystem:3.18.2")
    implementation("com.quarkdown.better-parse:better-parse:0.4.5")
    implementation("co.touchlab:kermit:2.2.0")
    implementation("com.mohamedrejeb.ksoup:ksoup-entities:0.6.0")
    implementation(libs.kotlinx.serialization.json)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    implementation("com.github.ajalt.colormath:colormath:3.7.0")
    implementation("com.quarkdown.bibliographer:bibliographer:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.5.2")
}
