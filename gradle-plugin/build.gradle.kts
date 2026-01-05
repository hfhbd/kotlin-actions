plugins {
    `kotlin-dsl`
    id("publish")
}

kotlin.jvmToolchain(17)

dependencies {
    implementation(projects.actionJson)
    compileOnly(projects.generator)

    implementation(libs.plugins.kotlin.multiplatform.toDep())
    implementation(libs.plugins.kotlin.plugin.js.plain.objects.toDep())
}

testing.suites.withType(JvmTestSuite::class).configureEach {
    useKotlinTest()
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

gradlePlugin.plugins.configureEach {
    displayName = "Gradle plugin to generate Kotlin entrypoints for GitHub actions.yml"
    description = "Gradle plugin to generate Kotlin entrypoints for GitHub actions.yml"
}

configurations.runtimeElements {
    attributes {
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, named("9.0"))
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
