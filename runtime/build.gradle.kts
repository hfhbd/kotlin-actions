plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        nodejs()
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
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
