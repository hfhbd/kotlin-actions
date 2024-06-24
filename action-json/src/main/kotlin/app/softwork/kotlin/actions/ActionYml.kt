package app.softwork.kotlin.actions

import kotlinx.serialization.Serializable

@Serializable
public data class ActionYml(
    val name: String,
    val author: String? = null,
    val description: String,
    val inputs: Map<String, Input> = emptyMap(),
    val outputs: Map<String, Output> = emptyMap(),
    val runs: Runs,
    val branding: Branding? = null,
)
