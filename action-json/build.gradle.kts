plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("publish")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

dependencies {
    api(libs.serialization.json)
}

testing.suites.withType(JvmTestSuite::class).configureEach {
    useKotlinTest()
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
