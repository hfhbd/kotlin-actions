import app.softwork.kotlin.actions.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.ir.*
import org.jetbrains.kotlin.gradle.targets.js.webpack.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
}

val workerActionDeps = configurations.dependencyScope("kotlinActions")

dependencies {
    workerActionDeps("app.softwork.kotlin.actions:generator:$VERSION")
}

val generateTypesafeAction by tasks.registering(GenerateTypesafeAction::class) {
    this.workerClasspath.from(configurations.resolvable("kotlinActionsWorkerClasspath") {
        extendsFrom(workerActionDeps.get())
    })
}

val actionFile = providers.of(ActionYmlSource::class) {
    parameters {
        actionFile.set(layout.projectDirectory.file("action.yml"))
    }
}

val customWebpackConfig = tasks.register("createCustomWebpackConfig", CreateCustomWebpackConfig::class) {
    nodeVersion.set(actionFile.map { it.runs.using.version.dropLastWhile { it != '.' }.dropLast(1) })
}

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

            val executable = tasks.register(
                "${name}Executable",
                KotlinWebpack::class,
                compilations.getByName("main"),
                project.objects
            )
            val sync = tasks.named("${name}ProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
            executable.configure {
                dependsOn(customWebpackConfig, sync)
                mode = KotlinWebpackConfig.Mode.PRODUCTION
                inputFilesDirectory.set(layout.dir(sync.flatMap { it.destinationDirectory }))
                entryModuleName.set(project.name + "-$name")
                esModules.set(true)
                outputDirectory.set(dir)
                output.globalObject = "this"
                mainOutputFileName.set(fileName)
                webpackConfigApplier {
                    configDirectory = customWebpackConfig.flatMap { it.outputDir.asFile }.get()
                }
            }
            customWebpackConfig {
                entry.set(executable.flatMap { it.entry })
            }

            tasks.assemble {
                dependsOn(executable)
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
