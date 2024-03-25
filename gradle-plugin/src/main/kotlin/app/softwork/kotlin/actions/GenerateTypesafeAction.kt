package app.softwork.kotlin.actions

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateTypesafeAction : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val actionFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        actionFile.convention(project.layout.projectDirectory.file("action.yml"))
        outputDirectory.convention(project.layout.buildDirectory.dir("actions/generated"))
    }

    @get:InputFiles
    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    fun generate() {
        workerExecutor.classLoaderIsolation {
            this.classpath.from(workerClasspath)
        }.submit(WorkerAction::class.java) {
            this.actionFile.set(this@GenerateTypesafeAction.actionFile)
            this.outputDirectory.set(this@GenerateTypesafeAction.outputDirectory)
        }
    }

    abstract class WorkerAction: WorkAction<WorkerAction.Gen> {
        interface Gen: WorkParameters {
            val actionFile: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            parameters.actionFile.get().asFile.generateCode(parameters.outputDirectory.get().asFile)
        }
    }
}
