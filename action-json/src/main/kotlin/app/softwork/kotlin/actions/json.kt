package app.softwork.kotlin.actions

import kotlinx.serialization.json.*

public val json: Json = Json {
    useAlternativeNames = false
}

public val replaceActionExpressions: Regex = """"\$\{\{[^}]*}}"""".toRegex()
