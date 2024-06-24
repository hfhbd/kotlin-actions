package app.softwork.kotlin.actions

import org.gradle.api.file.*
import org.gradle.api.provider.*

abstract class ActionYmlSource : ValueSource<ActionYml, ActionYmlSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val actionFile: RegularFileProperty
    }

    override fun obtain(): ActionYml {
        val text = parameters.actionFile.asFile.get().readText()

        return json.decodeFromString<ActionYml>(text.replace(replaceActionExpressions, "\"\""))
    }
}
