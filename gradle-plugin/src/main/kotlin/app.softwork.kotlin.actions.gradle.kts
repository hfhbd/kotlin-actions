import app.softwork.kotlin.actions.*
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.*
import org.jetbrains.kotlin.gradle.targets.js.webpack.*

plugins {
    kotlin("multiplatform")
}

val workerActionDeps = configurations.dependencyScope("kotlinActions")

dependencies {
    workerActionDeps("app.softwork.kotlin.actions:generator:$VERSION")
}

val kotlinActionsWorkerClasspath = configurations.resolvable("kotlinActionsWorkerClasspath") {
    extendsFrom(workerActionDeps.get())
}

val generateTypesafeAction by tasks.registering(GenerateTypesafeAction::class) {
    this.workerClasspath.from(kotlinActionsWorkerClasspath)
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
    applyDefaultHierarchyTemplate()

    js {
        binaries.executable()
        nodejs()
        useEsModules()

        compilerOptions {
            target.set("es2015")
            moduleKind.set(JsModuleKind.MODULE_ES)
            useEsClasses.set(true)
        }

        fun setUpTarget(
            name: String,
            file: Provider<String>,
        ) {
            val dir = layout.projectDirectory.dir(file.map { it.dropLastWhile { it != '/' } })
            val fileName = file.map { it.takeLastWhile { it != '/' } }

            val compilation = if (name == "main") {
                this.compilations.named("main")
            } else {
                this.compilations.register(name) {
                    val sourceName = name

                    compileTaskProvider {
                        compilerOptions.moduleName.set(sourceName)
                    }
                }
            }
            binaries.executable(compilation.get())
            compilation {
                val executable = tasks.register(
                    "${name}Executable",
                    KotlinWebpack::class,
                    this,
                )

                val sync: TaskProvider<out org.jetbrains.kotlin.gradle.tasks.IncrementalSyncTask> = if (name == "main") {
                    tasks.named("jsProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
                } else {
                    val bug = name.replaceFirstChar { it.uppercaseChar() }
                    tasks.named("js${bug}${bug}ProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
                }

                val fullName = outputModuleName

                executable.configure {
                    dependsOn(customWebpackConfig, sync)
                    mode = KotlinWebpackConfig.Mode.PRODUCTION
                    inputFilesDirectory.set(layout.dir(sync.flatMap { it.destinationDirectory }))
                    entryModuleName.set(fullName)
                    esModules.set(true)
                    outputDirectory.set(layout.buildDirectory.dir("actions/dist/$name"))
                    output.globalObject = "this"
                    mainOutputFileName.set(fileName)
                    sourceMaps = false
                    val configDir = customWebpackConfig.flatMap { it.outputDir.asFile }
                    webpackConfigApplier {
                        configDirectory = configDir.get()
                    }
                }
                tasks.register("copyAction${name}Dist", Copy::class) {
                    from(executable.flatMap { it.outputDirectory.file(fileName) })
                    into(dir)
                }
                val expectedWithoutTasksDependencyToNotRunCopy = objects.fileProperty().apply {
                    set(dir.flatMap { it.file(fileName) })
                }.locationOnly.map { it.asFile.absolutePath }
                val checkDist = tasks.register("check${name}Dist", CheckFileTask::class) {
                    actual.set(executable.flatMap { it.outputDirectory.file(fileName) })
                    expected.set(expectedWithoutTasksDependencyToNotRunCopy)
                    copyTaskPath.set(path.dropLastWhile { it != ':' } + "copyAction${name}Dist")
                }

                tasks.assemble {
                    dependsOn(executable)
                }
                tasks.check {
                    dependsOn(checkDist)
                }

                defaultSourceSet {
                    if (name == "main") {
                        kotlin.srcDirs(generateTypesafeAction)
                    } else {
                        dependsOn(this@kotlin.sourceSets.getByName("commonMain"))
                    }
                    dependencies {
                        implementation("app.softwork.kotlin.actions:runtime:$VERSION")
                    }
                }
            }
        }

        setUpTarget("main", actionFile.map { it.runs.main })

        val runs = actionFile.map { it.runs }
        val pre: Provider<String> = runs.map { it.pre }
        if (pre.isPresent) {
            setUpTarget("pre", pre)
        }
        val post: Provider<String> = runs.map { it.post }
        if (post.isPresent) {
            setUpTarget("post", post)
        }
    }
}

extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec> {
    // https://youtrack.jetbrains.com/issue/KT-65639
    version = actionFile.map { it.runs.using.version }.get()
}
