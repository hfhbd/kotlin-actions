package app.softwork.kotlin.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
