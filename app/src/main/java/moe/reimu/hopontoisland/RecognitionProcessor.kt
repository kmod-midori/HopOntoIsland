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
        val systemPrompt = """
            你是一个协助用户高效使用手机的 AI 智能助手，可以从屏幕显示的文字中提取出关键信息。
            
            首先，判断给出的内容是取餐码、排队等位还是打车，然后根据对应的分类，以JSON对象格式输出信息。
            
            取餐码字段：
            - critText，页面上醒目展示的取餐码/取餐号（通常为 1-6 位数字或短字母数字组合，如 "A12"、"#056"、"028"）。较长的订单号（如10位以上数字或字母）不是取餐码。
            - title，订单状态
            - content，商品名称、取餐门店名称
            - kind，固定为pickup

            排队等位字段：
            - critText，页面上醒目展示的排队编号（通常为纯数字、字母+数字、汉字+数字，如“12”、“A120”、“大厅12”）。
            - title，前方剩余人数或桌数，格式为“N桌”或“N人”，如果找不到则提取排队状态，如“待叫号”、“已就餐”。
            - content，排队门店名称、剩余时间
            - kind，固定为queue

            打车字段：
            - critText，页面上醒目展示的车牌号（一定是省份汉字+字母+数字，如“京AF0236”、“沪AA12347”）
            - title，行程进度，如“剩余x公里”、“剩余x分钟”，优先展示公里数，如果找不到则提取行程状态，如“行程中”、“接驾中”
            - content，司机姓名、打车服务商名称
            - kind，固定为taxi
            
            只输出最匹配的种类的信息，无法确定使用null。不要猜测文本中没有的信息。信息尽量简洁。仅返回JSON，不要输出Markdown、代码块或解释性文本。
        """.trimIndent()

        val modelResponse = ApiModelProvider.generate(
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