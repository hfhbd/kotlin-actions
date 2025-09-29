package app.softwork.kotlin.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://nodejs.org/en/about/previous-releases
@Serializable
public enum class Using(public val version: String) {
    @SerialName("node12")
    Node12("12.22.12"),

    @SerialName("node16")
    Node16("16.20.2"),

    @SerialName("node20")
    Node20("20.19.4"),

    @SerialName("node24")
    Node24("24.5.0"),
}
