package app.softwork.kotlin.actions

import kotlinx.serialization.Serializable

@Serializable
public data class ActionYml(
    val name: String,
    val author: String? = null,
    val description: String,
    val inputs: Map<String, Input>? = null,
    val outputs: Map<String, Output>? = null,
    val runs: Runs,
    val branding: Branding? = null,
)
