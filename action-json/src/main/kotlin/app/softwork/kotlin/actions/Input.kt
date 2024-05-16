package app.softwork.kotlin.actions

import kotlinx.serialization.Serializable

@Serializable
public data class Input(
    val description: String,
    val required: Boolean = false,
    val default: String? = null,
    val deprecationMessage: String? = null,
)
