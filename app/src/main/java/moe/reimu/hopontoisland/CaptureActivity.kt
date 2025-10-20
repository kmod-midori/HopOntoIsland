package moe.reimu.hopontoisland

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import moe.reimu.hopontoisland.ui.theme.DarkColorScheme
import moe.reimu.hopontoisland.ui.theme.LightColorScheme

class CaptureActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        (GlobalScope + Dispatchers.IO).launch {
            try {
                doCapture()
            } catch (e: Exception) {
                Log.e(TAG, "doCapture failed", e)
                postErrorNotification(e.message.orEmpty())
            }
        }

        finish()
    }

    suspend fun doCapture() {
        val serviceInstance = TextAccessibilityService.getInstance()
        if (serviceInstance == null) {
            throw IllegalStateException("serviceInstance is null")
        }

        postLiveUpdate(
            this,
            "识别中",
            "正在识别",
            "",
            R.drawable.ic_search,
            "#3DDC84".toColorInt(),
        )

        val dumpedText = try {
            serviceInstance.captureActiveWindowText().joinToString(" ")
        } catch (e: Exception) {
            throw RuntimeException("Failed to obtain a11y text", e)
        }
        Log.i(TAG, "text = [$dumpedText]")

        val entity = RecognitionProcessor.recognizeText(dumpedText)
        if (entity == null) {
            throw RuntimeException("Failed to recognize entity")
        }

        val icon = when (entity.kind) {
            "dining" -> {
                R.drawable.ic_dining
            }

            "taxi" -> {
                R.drawable.ic_taxi
            }

            else -> {
                R.drawable.ic_info
            }
        }

        postLiveUpdate(
            this,
            entity.critText,
            "${entity.critText} | ${entity.title}",
            entity.content.orEmpty(),
            icon,
            "#3DDC84".toColorInt()
        )
    }

    fun postErrorNotification(content: String) {
        val colorScheme =
            if (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        val errorColor = colorScheme.errorContainer
        postLiveUpdate(
            this,
            "识别失败",
            "识别失败",
            content,
            R.drawable.ic_warning,
            Color.valueOf(errorColor.red, errorColor.green, errorColor.blue).toArgb()
        )
    }

    companion object {
        private const val TAG = "CaptureActivity"
    }
}