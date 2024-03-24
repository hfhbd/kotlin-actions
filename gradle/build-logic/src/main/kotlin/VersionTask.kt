import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

@CacheableTask
abstract class VersionTask : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    init {
        version.convention(project.version.toString())
    }

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        outputFolder.convention(project.layout.buildDirectory.dir("generated/version"))
    }

    @TaskAction
    fun generate() {
        File(outputFolder.asFile.get(), "version.kt").apply {
            if (!exists()) {
                createNewFile()
            }
        }.writeText(
            """
            |package app.softwork.typesafe.github.actions
            |
            |internal val VERSION: String = "${version.get()}"
            |
            """.trimMargin(),
        )
    }
}
