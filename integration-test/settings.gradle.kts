pluginManagement {
    includeBuild("..")
    includeBuild("../gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("myRepos")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "integration-test"

includeBuild("..")
