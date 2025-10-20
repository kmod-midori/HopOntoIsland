package moe.reimu.hopontoisland

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import moe.reimu.hopontoisland.llm.ApiModelProvider
import moe.reimu.hopontoisland.model.ModelMessage
import moe.reimu.hopontoisland.model.ModelRequest
import moe.reimu.hopontoisland.model.RecognizedEntity
import org.json.JSONObject

object RecognitionProcessor {
    @OptIn(ExperimentalSerializationApi::class)
    private val entityFormat = Json {
        ignoreUnknownKeys = true
    }

    private const val TAG = "RecognitionProcessor"

    suspend fun recognizeText(text: String): RecognizedEntity? {
        val systemPromptJson = JSONObject().apply {
            put("critText", "取餐号/排位号/取件码/车牌号等，不超过10个字符，不带描述")
            put("title", "通知的简要标题")
            put("content", "通知详情，包含商品名称、店名、地址、时间等")
            put("kind", "实时事件的类型，从 dining/taxi/general 中选择一个")
        }

        val systemPrompt = """
            你是一个协助用户高效使用手机的 AI 智能助手，可以从屏幕显示的文字中提取出关键信息。
            
            分析提供的文本，并创建即时通知，输出必须严格遵循以下 JSON 格式：
            $systemPromptJson
        """.trimIndent()

        val modelResponse = ApiModelProvider().generate(
            ModelRequest(
                model = "(placeholder)",
                temperature = 0.1,
                responseFormat = ModelRequest.ResponseFormat("json_object"),
                messages = listOf(
                    ModelMessage("system", systemPrompt),
                    ModelMessage("user", text)
                )
            )
        )

        return try {
            entityFormat.decodeFromString(modelResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode entity", e)
            null
        }
    }
}