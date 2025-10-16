import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("publish")
}

val writeToken by tasks.registering(GetTokenTask::class) {
    token.set(providers.gradleProperty("token"))
}

kotlin {
    js {
        nodejs()
        useEsModules()

        compilerOptions {
            target.set("es2015")
        }
    }

    sourceSets {
        jsMain {
            dependencies {
                api(libs.ktor.client.js)
                api(kotlinWrappers.node)
            }
        }
        jsTest {
            kotlin.srcDir(writeToken)
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
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
