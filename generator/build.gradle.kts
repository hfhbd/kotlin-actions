plugins {
    kotlin("jvm")
    id("publish")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

dependencies {
    implementation(projects.actionJson)
    implementation(libs.kotlinpoet)

    testImplementation(kotlin("test"))
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
