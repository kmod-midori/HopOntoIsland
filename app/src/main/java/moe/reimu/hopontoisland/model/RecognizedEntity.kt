package moe.reimu.hopontoisland.model

import kotlinx.serialization.Serializable

@Serializable
data class RecognizedEntity(
    val critText: String,
    val title: String,
    val content: String? = null,
    val kind: String? = null,
)
