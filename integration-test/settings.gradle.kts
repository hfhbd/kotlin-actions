pluginManagement {
    includeBuild("..")
    includeBuild("../gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

plugins {
    id("myRepos")
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
