dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }

    versionCatalogs.register("libs") {
        from(files("../libs.versions.toml"))
    }
}

rootProject.name = "build-logic"
