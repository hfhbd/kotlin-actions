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
        jsMain {
            dependencies {
                api(libs.coroutines.core)
                api(libs.kotlin.wrappers.actions.toolkit)
            }
        }
        jsTest {
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
