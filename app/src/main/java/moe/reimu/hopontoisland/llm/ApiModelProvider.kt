package moe.reimu.hopontoisland.llm

import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.ThinkingConfig
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
import moe.reimu.hopontoisland.model.ModelResponse
import moe.reimu.hopontoisland.model.ModelRequest

object ApiModelProvider : ModelProvider {
    private val provider: String
    private val url: String
    private val apiKey: String
    private val modelName: String

    init {
        val settings = MyApplication.getInstance().getSettings()
        provider = settings.modeProvider!!
        url = settings.modelUrl!!
        apiKey = settings.modelKey!!
        modelName = if(provider == "Gemini") settings.modelName ?: "gemini-2.5-flash" else settings.modelName!!
    }

    override suspend fun generate(request: Any?): String {
        if(provider == "Gemini"){
            val client = Client.builder().apiKey(apiKey).build()

            val config =
                GenerateContentConfig.builder() // Disables thinking
                    .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0))
                    .build()

            if(request is String){
                val response =
                    client.models.generateContent(modelName, request, config)

                if (response.text().isNullOrEmpty()) {
                    throw RuntimeException("Model request failed")
                }

                return response.text() ?: ""
            }
        }
        else if(provider == "OpenAI"){
            if(request is ModelRequest){
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

        return  ""
    }
}