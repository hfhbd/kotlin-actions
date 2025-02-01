# kotlin-actions

Write GitHub actions in Kotlin.

## Install

This package is uploaded to `mavenCentral`.

````kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
    }
}

// build.gradle.kts
plugins {
    id("app.softwork.kotlin.actions") version "LATEST"
}
````

## Development
```sh
rm -f kotlin-js-store/package-lock.json && ./gradlew clean kotlinUpgradePackageLock build
rm -f integration-test/kotlin-js-store/package-lock.json && ./gradlew clean kotlinUpgradePackageLock copyActionpostDist copyActionmainDist build -pintegration-test --scan 
```
