package moe.reimu.hopontoisland.llm

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
import moe.reimu.hopontoisland.model.ModelRequest
import moe.reimu.hopontoisland.model.ModelResponse

class ApiModelProvider : ModelProvider {
    private val url: String
    private val apiKey: String
    private val modelName: String

    init {
        val settings = MyApplication.getInstance().getSettings()
        url = settings.modelUrl!!
        apiKey = settings.modelKey!!
        modelName = settings.modelName!!
    }

    override suspend fun generate(request: ModelRequest): String {
        val response = MyApplication.getInstance().ktorClient.post {
            url(this@ApiModelProvider.url)
            contentType(ContentType.Application.Json)
            bearerAuth(this@ApiModelProvider.apiKey)
            setBody(request.copy(model = this@ApiModelProvider.modelName))
        }
        if (!response.status.isSuccess()) {
            val rawResponse = response.bodyAsText()
            throw RuntimeException("Model request failed (${response.status}): $rawResponse")
        }
        val modelResponse: ModelResponse = response.body()
        return modelResponse.choices.first().message.content
    }
}