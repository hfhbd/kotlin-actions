import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

@CacheableTask
abstract class GetTokenTask : DefaultTask() {
    @get:Input
    abstract val token: Property<String>

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    init {
        outputFolder.convention(project.layout.buildDirectory.dir("generated/token"))
    }

    @TaskAction
    fun generate() {
        File(outputFolder.asFile.get(), "token.kt").writeText(
            """
            |package app.softwork.kotlin.actions
            |
            |internal val GITHUB_TOKEN: String = "${token.get()}"
            |
            """.trimMargin(),
        )
    }
}
