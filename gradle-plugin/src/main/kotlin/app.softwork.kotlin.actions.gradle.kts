import app.softwork.kotlin.actions.*
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.ir.*
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.webpack.*

plugins {
    kotlin("multiplatform")
    id("app.softwork.kotlin.actions.typed")
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

            freeCompilerArgs.addAll(
                "-Xes-long-as-bigint",
                "-Xdont-warn-on-error-suppression",
                "-Xgenerate-polyfills=false",
                "-Xir-generate-inline-anonymous-functions",
                "-Xwarning-level=NOTHING_TO_INLINE:disabled",
            )
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
                val compilation = this
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
                    dependsOn(tasks.named("kotlinNodeJsSetup"))
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
                    @Suppress("INVISIBLE_REFERENCE")
                    getIsWasm.set(false)
                    @Suppress("INVISIBLE_REFERENCE")
                    this.versions.set(rootProject.kotlinNodeJsRootExtension.versions)
                    val npmToolingDir = project.objects.directoryProperty().fileProvider(compilation.npmProject.dir.map { it.asFile })

                    @Suppress("INVISIBLE_REFERENCE")
                    this.npmToolingEnvDir.set(npmToolingDir)
                    webpackConfigApplier {
                        configDirectory = configDir.get()
                    }
                }
                val copyDist = tasks.register("copyAction${name}Dist", Copy::class) {
                    from(executable.flatMap { it.outputDirectory.file(fileName) })
                    into(dir)
                }

                tasks.assemble {
                    dependsOn(copyDist)
                }

                defaultSourceSet {
                    if (name == "main") {
                        kotlin.srcDirs(tasks.named("generateTypesafeAction"))
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
    version.set(actionFile.map { it.runs.using.version })
}
