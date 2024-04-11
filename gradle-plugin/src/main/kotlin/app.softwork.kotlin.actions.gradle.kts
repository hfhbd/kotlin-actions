import app.softwork.kotlin.actions.*
import org.jetbrains.kotlin.gradle.dsl.*
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

val actionFile = providers.of(ActionYmlSource::class.java) {
    parameters {
        actionFile.set(layout.projectDirectory.file("action.yml"))
    }
}
// https://youtrack.jetbrains.com/issue/KT-62248
// properties.put("kotlin.js.ir.output.granularity", "whole-program")

kotlin {
    fun setUpTarget(
        name: String,
        dependsOn: Boolean,
        file: Provider<String>,
    ) {
        val dir = layout.projectDirectory.dir(file.map { it.dropLastWhile { it != '/' } })
        val fileName = file.map { it.takeLastWhile { it != '/' } }

        js(name) {
            binaries.executable()
            nodejs()
            useEsModules()

            compilerOptions {
                moduleKind.set(JsModuleKind.MODULE_ES)
                useEsClasses.set(true)

                moduleName.set(fileName)

                sourceMap.set(false)
                sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_NEVER)
            }
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
                    .flatMap { it.destinationDirectory }) {
                exclude {
                    it.name.endsWith(".map")
                }
            }
            into(dir)
        }

        tasks.assemble {
            dependsOn(copyDist)
        }
    }

    setUpTarget("main", true, actionFile.map { it.runs.main })

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
