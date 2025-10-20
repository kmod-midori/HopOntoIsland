package moe.reimu.hopontoisland.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelRequest(
    val model: String,
    val messages: List<ModelMessage>,
    val stream: Boolean = false,
    val temperature: Double? = null,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
) {
    @Serializable
    data class ResponseFormat(val type: String)
}
