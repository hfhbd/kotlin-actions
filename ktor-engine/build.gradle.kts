import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    js {
        nodejs()
        useEsModules()

        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_ES)
            useEsClasses.set(true)
        }
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                api(libs.ktor.client.js)
                api("org.jetbrains.kotlin-wrappers:kotlin-node:20.11.30-pre.751")
            }
        }
    }
}

val emptyJar by tasks.registering(Jar::class)

publishing {
    publications.withType(MavenPublication::class).configureEach {
        artifact(emptyJar) {
            classifier = "javadoc"
        }
    }
}
