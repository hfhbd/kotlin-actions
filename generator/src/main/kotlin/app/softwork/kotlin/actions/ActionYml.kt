package app.softwork.kotlin.actions

import kotlinx.serialization.*

@Serializable
public data class ActionYml(
    val name: String,
    val description: String,
    val inputs: Map<String, Input>? = null,
    val outputs: Map<String, Output>? = null,
    val runs: Runs,
)

@Serializable
public data class Runs(
    val using: Using,
    val main: String,
)

// https://nodejs.org/en/about/previous-releases
@Serializable
public enum class Using(val version: String) {
    @SerialName("node12")
    Node12("12.22.12"),

    @SerialName("node16")
    Node16("16.20.2"),

    @SerialName("node20")
    Node20("20.11.0"),
}

@Serializable
public data class Input(
    val description: String,
    val required: Boolean,
    val default: String? = null,
)

@Serializable
public data class Output(
    val description: String,
)
