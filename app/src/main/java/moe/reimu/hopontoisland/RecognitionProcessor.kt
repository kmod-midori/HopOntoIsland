package moe.reimu.hopontoisland

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
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

    val settings = MyApplication.getInstance().getSettings()

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
            $systemPromptJson,
            提供的文本如下：
            $text
        """.trimIndent()

        val request = if(settings.modeProvider == "Gemini") systemPrompt
        else if(settings.modeProvider == "OpenAI")
            ModelRequest(
            model = "(placeholder)",
            temperature = 0.1,
            responseFormat = ModelRequest.ResponseFormat("json_object"),
            messages = listOf(
                ModelMessage("system", systemPrompt),
                ModelMessage("user", text)
            )
        )
        else
            null

        val modelResponse: String = ApiModelProvider.generate(
            request
        )

        Log.d(TAG, "modelResponse $modelResponse")

        var response:String? = null

        if(settings.modeProvider == "Gemini"){
            response =  extractAndCompactJson(modelResponse)
            Log.d(TAG, "Gemini response $response")
        }
        else if(settings.modeProvider == "OpenAI"){
            response = modelResponse
            Log.d(TAG, "OpenAI response $response")
        }

        if(response == null) return null

        return try {
            entityFormat.decodeFromString(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode entity", e)
            null
        }
    }

    /**
     * 从包含多行空白字符的字符串中提取 JSON 对象，并将其格式化为单行（不带换行）。
     *
     * @param input 包含 JSON 的原始多行字符串。
     * @return 提取并压缩后的单行 JSON 字符串，如果未找到则返回 null。
     */
    fun extractAndCompactJson(input: String): String? {

        // 正则表达式：
        // 1. \\{：匹配左大括号 {
        // 2. [\\s\\S]*?：
        //    - [\\s\\S] 匹配任何空白字符 (\s) 或任何非空白字符 (\S)，即匹配所有字符，包括换行符。
        //    - *? 使用非贪婪模式，匹配零次或多次。
        // 3. \\}：匹配右大括号 }
        // 使用这个模式确保匹配能跨越换行符。
        val regex = "\\{[\\s\\S]*?\\}"

        // 1. 提取 JSON 字符串片段
        val jsonStringWithWhitespace = Regex(regex).find(input)?.value

        if (jsonStringWithWhitespace != null) {
            // 2. 移除提取出的 JSON 字符串中的所有换行符、制表符和多余的空格，
            //    将其压缩成一个紧凑的单行 JSON 字符串。
            // \\s+ 匹配一个或多个空白字符（包括空格、换行、制表符等）。
            // .replace(Regex("\\s+"), "") 会把所有空白都移除，但对于 JSON 字符串内部的值，
            // 比如 "取餐 提醒"（中间带空格）会变成 "取餐提醒"，这可能会破坏数据。

            // **最安全的做法是只移除 JSON 结构符号周围的换行和多余空格，而不是移除所有空格。**
            // 但由于您的 JSON 示例中键值对都是用双引号括起来的，我们通常可以直接移除所有换行和结构空格：

            // 移除所有换行符 \n, \r, \t
            var compactedJson = jsonStringWithWhitespace.replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")

            // 移除键值对符号外的多余空格 (例如 { "key" : "value" } -> {"key":"value"})
            // 使用更精确的正则替换
            compactedJson = compactedJson.replace(Regex("\\s*([\\{\\}:,\\[\\]])\\s*"), "$1")
                .trim()

            return compactedJson
        } else {
            return null
        }
    }
}