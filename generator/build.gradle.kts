plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.kotlinpoet)

    testImplementation(kotlin("test"))
}
