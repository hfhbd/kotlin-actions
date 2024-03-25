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

val actionFile = providers.of(ActionYmlSource::class.java) {}

kotlin {
    fun setUpTarget(
        name: String,
        dependsOn: Boolean,
        file: Provider<String>,
    ) {
        js(name) {
            binaries.executable()
            nodejs()
        }
        sourceSets {
            named("${name}Main") {
                if (dependsOn) {
                    kotlin.srcDirs(generateTypesafeAction)
                }
                dependencies {
                    implementation("app.softwork.kotlin.actions:runtime:$VERSION")
                }
            }
        }
        val copyDist = tasks.register("copy${name}Dist", Copy::class) {
            from(
                tasks.named("${name}ProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
                    .flatMap { it.destinationDirectory })
            into(layout.projectDirectory.dir(file))
        }

        tasks.assemble {
            dependsOn(copyDist)
        }
    }

    setUpTarget("js", true, actionFile.map { it.runs.main })

    val runs = actionFile.map { it.runs }
    val pre: Provider<String> = runs.map { it.pre }
    if (pre.isPresent) {
        setUpTarget("pre", false, pre)
    }
    val post: Provider<String> = runs.map { it.post }
    if (post.isPresent) {
        setUpTarget("post", false, post)
    }
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    // https://youtrack.jetbrains.com/issue/KT-65639
    version = actionFile.map { it.runs.using.version }.get()
}
