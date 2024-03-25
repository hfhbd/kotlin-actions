package app.softwork.kotlin.actions

import org.gradle.api.file.*
import org.gradle.api.provider.*
import javax.inject.*

abstract class ActionYmlSource : ValueSource<ActionYml, ActionYmlSource.Parameters> {
    abstract class Parameters @Inject constructor(
        layout: ProjectLayout
    ) : ValueSourceParameters {
        abstract val file: RegularFileProperty

        init {
            file.convention(layout.projectDirectory.file("action.yml"))
        }
    }

    override fun obtain(): ActionYml {
        val s = json.decodeFromString<ActionYml>(parameters.file.asFile.get().readText())
        return s
    }
}
