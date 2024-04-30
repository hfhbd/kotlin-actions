import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
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
                api("org.jetbrains.kotlin-wrappers:kotlin-node:20.11.30-pre.736")
                api(npm("@actions/github", "6.0.0"))
                api(npm("@actions/core", "1.10.1"))
            }
        }
        named("jsTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
