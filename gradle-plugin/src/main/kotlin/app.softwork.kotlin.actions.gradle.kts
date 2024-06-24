import app.softwork.kotlin.actions.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.ir.*
import org.jetbrains.kotlin.gradle.targets.js.webpack.*

plugins {
    kotlin("multiplatform")
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

val gitHubActionStepAttribute = Attribute.of("GitHubActionStep", String::class.java)

kotlin {
    fun setUpTarget(
        name: String,
        dependsOn: Boolean,
        file: Provider<String>,
    ) {
        val dir = layout.projectDirectory.dir(file.map { it.dropLastWhile { it != '/' } })
        val fileName = file.map { it.takeLastWhile { it != '/' } }

        val fullName = project.rootProject.name + if (project === project.rootProject) {
            "-$name"
        } else {
            project.path.replace(':', '-') + '-' + name
        }

        js(name) {
            attributes.attribute(gitHubActionStepAttribute, name)
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
            )
            val sync = tasks.named("${name}ProductionExecutableCompileSync", DefaultIncrementalSyncTask::class)
            val customWebpackConfig =
                tasks.register("createCustomWebpackConfig${name}", CreateCustomWebpackConfig::class)
            executable.configure {
                dependsOn(customWebpackConfig, sync)
                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
                inputFilesDirectory.set(layout.dir(sync.flatMap { it.destinationDirectory }))
                entryModuleName.set(fullName)
                esModules.set(true)
                outputDirectory.set(layout.buildDirectory.dir("actions/dist/$name"))
                mainOutputFileName.set(fileName)
                sourceMaps = false
                webpackConfigApplier {
                    configDirectory = customWebpackConfig.flatMap { it.outputDir.asFile }.get()
                }
            }
            customWebpackConfig {
                entryFileName.set(fileName)
                outputDir.set(project.layout.buildDirectory.dir("actions/webpack/$name"))
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
