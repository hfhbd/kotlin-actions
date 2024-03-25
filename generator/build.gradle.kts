plugins {
    kotlin("jvm")
    id("friendPath")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

dependencies {
    implementation(projects.actionJson)
    friendPath(projects.actionJson)
    implementation(libs.kotlinpoet)

    testImplementation(kotlin("test"))
}
