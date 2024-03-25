package app.softwork.kotlin.actions

import kotlinx.serialization.*

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

@Serializable
public data class Runs(
    val using: Using,
    val pre: String? = null,
    @SerialName("pre-if")
    val preIf: String? = null,
    val main: String,
    val post: String? = null,
    @SerialName("post-if")
    val postIf: String? = null,
)

// https://nodejs.org/en/about/previous-releases
@Serializable
public enum class Using(public val version: String) {
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
    val deprecationMessage: String? = null,
)

@Serializable
public data class Output(
    val description: String,
)

@Serializable
public data class Branding(
    val color: Color,
    val icon: String,
) {
    @Serializable
    public enum class Color {
        @SerialName("white")
        White,

        @SerialName("yellow")
        Yellow,

        @SerialName("blue")
        Blue,

        @SerialName("green")
        Green,

        @SerialName("orange")
        Orange,

        @SerialName("red")
        Red,

        @SerialName("purple")
        Purple,

        @SerialName("gray-dark")
        GrayDark,
    }
}
