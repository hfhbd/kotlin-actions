plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

dependencies {
    api(libs.serialization.json)
}
