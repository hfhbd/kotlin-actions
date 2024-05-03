plugins {
    id("app.softwork.kotlin.actions")
    kotlin("plugin.serialization")
}

val writeToken by tasks.registering(GetTokenTask::class) {
    token.set(providers.gradleProperty("token"))
}

kotlin.sourceSets {
    mainMain {
        dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }
    mainTest {
        kotlin.srcDir(writeToken)
        dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        }
    }
}
