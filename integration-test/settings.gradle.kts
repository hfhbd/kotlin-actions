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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("com.gradle.develocity") version "3.19.1"
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

dependencyResolutionManagement {
    versionCatalogs.register("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}

rootProject.name = "integration-test"

includeBuild("..")

include(":sub")
include(":failure")
