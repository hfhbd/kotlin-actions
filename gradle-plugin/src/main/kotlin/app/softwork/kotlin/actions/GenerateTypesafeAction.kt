package app.softwork.kotlin.actions

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateTypesafeAction : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val actionFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            this.classpath.from(workerClasspath)
        }.submit(WorkerAction::class) {
            this.actionFile.set(this@GenerateTypesafeAction.actionFile)
            this.outputDirectory.set(this@GenerateTypesafeAction.outputDirectory)
        }
    }

    internal abstract class WorkerAction: WorkAction<WorkerAction.Gen> {
        interface Gen: WorkParameters {
            val actionFile: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            parameters.actionFile.get().asFile.generateCode(parameters.outputDirectory.get().asFile)
        }
    }
}
