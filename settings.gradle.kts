pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("myRepos")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.develocity") version "3.17.5"
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        val isCI = providers.environmentVariable("CI").isPresent
        publishing {
            onlyIf { isCI }
        }
        tag("CI")
    }
}

rootProject.name = "kotlin-actions"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":action-json")
include(":generator")
include(":gradle-plugin")
include(":runtime")
include(":ktor-engine")
