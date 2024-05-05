package app.softwork.kotlin.actions

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Input
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Not worth caching")
abstract class CheckFileTask : DefaultTask() {
    @get:Input
    abstract val expected: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val actual: RegularFileProperty

    @get:Input
    internal abstract val copyTaskPath: Property<String>

    @TaskAction
    fun checkContent() {
        val expected = File(expected.get())
        require(expected.exists()) {
            "$expected not found. Did you forgot to call ${copyTaskPath.get()}?"
        }
        require(expected.readText() == actual.asFile.get().readText()) {
            "$expected does not match ${actual.asFile.get()}. Did you forgot to call ${copyTaskPath.get()}?"
        }
    }
}
