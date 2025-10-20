package moe.reimu.hopontoisland.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelMessage(val role: String, val content: String)
