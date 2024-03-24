plugins {
    kotlin("js")
}

kotlin {
    js {
        nodejs()
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api(npm("@actions/github", "6.0.0"))
    api(npm("@actions/core", "1.10.1"))

    testImplementation(kotlin("test"))
}
