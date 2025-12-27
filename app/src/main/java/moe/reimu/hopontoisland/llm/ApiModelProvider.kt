package moe.reimu.hopontoisland.llm

import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import moe.reimu.hopontoisland.MyApplication
import moe.reimu.hopontoisland.R
import moe.reimu.hopontoisland.model.ModelRequest
import moe.reimu.hopontoisland.model.ModelResponse

object ApiModelProvider : ModelProvider {
    override suspend fun generate(request: ModelRequest): String {
        val settings = MyApplication.getInstance().getSettings()
        val url = when (settings.modelProvider) {
            "openai" -> {
                settings.modelUrl
            }
            else -> {
                apiProviders[settings.modelProvider]?.completionUrl
            }
        }
        val apiKey = settings.modelKey
        val modelName = settings.modelName
        if (url.isNullOrBlank() || apiKey.isNullOrBlank() || modelName.isNullOrBlank()) {
            throw IllegalStateException("Model API not configured")
        }

        val response = MyApplication.getInstance().ktorClient.post {
            url(url)
            contentType(ContentType.Application.Json)
            bearerAuth(apiKey)
            setBody(request.copy(model = modelName))
        }
        if (!response.status.isSuccess()) {
            val rawResponse = response.bodyAsText()
            throw RuntimeException("Model request failed (${response.status}): $rawResponse")
        }
        val modelResponse: ModelResponse = response.body()
        return modelResponse.choices.first().message.content
    }

    data class ApiProvider(
        val displayNameRes: Int,
        val completionUrl: String
    )

    val apiProviders = mapOf(
        "openai" to ApiProvider(R.string.model_provider_openai, ""),
        "gemini" to ApiProvider(R.string.model_provider_gemini, "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"),
        "aliyun_cn" to ApiProvider(R.string.model_provider_aliyun_cn, "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
    )
}