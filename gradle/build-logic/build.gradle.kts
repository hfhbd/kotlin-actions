plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.multiplatform.toDep())
    implementation(libs.plugins.kotlin.plugin.serialization.toDep())
    implementation(libs.plugins.kotlin.plugin.js.plain.objects.toDep())
    implementation(libs.plugins.mavencentral.toDep())
    implementation(libs.plugins.foojay.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
