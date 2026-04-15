pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("myRepos")
}

dependencyResolutionManagement {
    versionCatalogs {
        register("kotlinWrappers") {
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:2026.4.8")
        }
    }
}

rootProject.name = "kotlin-actions"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":action-json")
include(":generator")
include(":gradle-plugin")
include(":runtime")
include(":ktor-nodejs-client-engine")
