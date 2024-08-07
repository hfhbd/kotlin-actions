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
                api(libs.coroutines.core)
                api(npm("@actions/github", "6.0.0"))
                api("org.jetbrains.kotlin-wrappers:kotlin-actions-toolkit:1.0.0-pre.786")
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
