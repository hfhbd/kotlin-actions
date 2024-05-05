import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                api(npm("@actions/github", "6.0.0"))
                api("org.jetbrains.kotlin-wrappers:kotlin-actions-toolkit:0.0.1-pre.738")
            }
        }
        named("jsTest") {
            dependencies {
                implementation(kotlin("test"))
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
