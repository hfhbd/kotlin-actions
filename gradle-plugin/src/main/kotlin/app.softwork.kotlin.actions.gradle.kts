import app.softwork.typesafe.github.actions.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.gradle.targets.js.ir.*

plugins {
    kotlin("js")
    kotlin("plugin.js-plain-objects")
}

val generateTypesafeAction by tasks.registering(GenerateTypesafeAction::class)

kotlin {
    js {
        binaries.executable()
        nodejs()
    }
    sourceSets {
        named("main") {
            kotlin.srcDirs(generateTypesafeAction)
            dependencies {
                implementation("app.softwork.kotlin.actions:runtime:$VERSION")
            }
        }
    }
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    val json = Json {
        ignoreUnknownKeys = true
    }
    version = providers.fileContents(layout.projectDirectory.file("action.yml")).asText.map {
        json.decodeFromString<ActionYml>(it).runs.using.version
    }.get()
}

val copyDist by tasks.registering(Copy::class) {
    from(tasks.named("productionExecutableCompileSync", DefaultIncrementalSyncTask::class).flatMap { it.destinationDirectory })
    into(layout.projectDirectory.dir("dist"))
}

tasks.assemble {
    dependsOn(copyDist)
}
