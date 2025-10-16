plugins {
    id("app.softwork.kotlin.actions")
    kotlin("plugin.serialization")
}

val writeToken by tasks.registering(GetTokenTask::class) {
    token.set(providers.gradleProperty("token"))
}

kotlin.sourceSets {
    jsMain {
        dependencies {
            implementation("app.softwork.kotlin.actions:ktor-nodejs-client-engine")
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }
    jsTest {
        kotlin.srcDir(writeToken)
        dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}
