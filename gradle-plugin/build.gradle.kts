plugins {
    `kotlin-dsl`
}

kotlin.jvmToolchain(8)

dependencies {
    compileOnly(projects.generator)
    implementation(libs.plugins.kotlin.multiplatform.toDep())
    implementation(libs.plugins.kotlin.js.plain.objects.toDep())

    testImplementation(kotlin("test"))
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

val generateVersion by tasks.registering(VersionTask::class)

sourceSets.main {
    kotlin.srcDir(generateVersion)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
