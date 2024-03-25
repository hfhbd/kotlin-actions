plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.kotlinpoet)

    testImplementation(kotlin("test"))
}
