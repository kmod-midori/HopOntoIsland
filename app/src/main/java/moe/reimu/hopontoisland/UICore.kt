package moe.reimu.hopontoisland

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.toColorInt
import moe.reimu.hopontoisland.ui.theme.DarkColorScheme
import moe.reimu.hopontoisland.ui.theme.LightColorScheme

object UICore {
    suspend fun doCapture(context: Context) {
        val serviceInstance = TextAccessibilityService.getInstance()
        if (serviceInstance == null) {
            throw IllegalStateException("serviceInstance is null")
        }

        postLiveUpdate(
            context,
            context.getString(R.string.noti_recognizing),
            context.getString(R.string.noti_in_recognizing),
            "",
            R.drawable.ic_search,
            "#3DDC84".toColorInt(),
        )

        val dumpedText = try {
            serviceInstance.captureActiveWindowText().joinToString(" ")
        } catch (e: Exception) {
            throw RuntimeException("Failed to obtain a11y text", e)
        }
        Log.d("Core", "text = [$dumpedText]")

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
            context,
            entity.critText,
            "${entity.critText} | ${entity.title}",
            entity.content.orEmpty(),
            icon,
            "#3DDC84".toColorInt()
        )
    }

    fun postErrorNotification(context: Context, content: String) {
        val colorScheme =
            if (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        val errorColor = colorScheme.errorContainer
        postLiveUpdate(
            context,
            context.getString(R.string.noti_error),
            context.getString(R.string.noti_error),
            content,
            R.drawable.ic_warning,
            Color.valueOf(errorColor.red, errorColor.green, errorColor.blue).toArgb()
        )
    }
}