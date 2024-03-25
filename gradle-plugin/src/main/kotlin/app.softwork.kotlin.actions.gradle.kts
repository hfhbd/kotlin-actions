import app.softwork.kotlin.actions.*
import org.jetbrains.kotlin.gradle.targets.js.ir.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
}

val workerActionDeps = configurations.dependencyScope("kotlinActions")
val workerActionClasspath = configurations.resolvable("kotlinActionsWorkerClasspath") {
    extendsFrom(workerActionDeps.get())
}

dependencies {
    workerActionDeps("app.softwork.kotlin.actions:generator:$VERSION")
}

val generateTypesafeAction by tasks.registering(GenerateTypesafeAction::class) {
    this.workerClasspath.from(workerActionClasspath)
}

kotlin {
    js {
        binaries.executable()
        nodejs()
    }
    sourceSets {
        named("jsMain") {
            kotlin.srcDirs(generateTypesafeAction)
            dependencies {
                implementation("app.softwork.kotlin.actions:runtime:$VERSION")
            }
        }
    }
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
// TODO: read the value from actions.yml
    version = "20.11.0"
}

val copyDist by tasks.registering(Copy::class) {
    from(
        tasks.named("jsProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
            .flatMap { it.destinationDirectory })
    into(layout.projectDirectory.dir("dist"))
}

tasks.assemble {
    dependsOn(copyDist)
}
