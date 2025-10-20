package moe.reimu.hopontoisland.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelResponse(val choices: List<Choice>, val usage: Usage) {
    @Serializable
    data class Choice(val message: ModelMessage)

    @Serializable
    data class Usage(
        @SerialName("prompt_tokens") val promptTokens: Int,
        @SerialName("total_tokens") val totalTokens: Int,
        @SerialName("completion_tokens") val completionTokens: Int,
    )
}
