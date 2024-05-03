package app.softwork.kotlin.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
