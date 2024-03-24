plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.js.toDep())
    implementation(libs.plugins.publish.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
