plugins {
    id("app.softwork.kotlin.actions")
}

kotlin.sourceSets.mainTest {
    dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    }
}
