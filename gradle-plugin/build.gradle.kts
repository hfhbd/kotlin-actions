plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.kotlinpoet)
    implementation(libs.plugins.kotlin.js.toDep())
    implementation(libs.plugins.kotlin.jsplain.objects.toDep())
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
