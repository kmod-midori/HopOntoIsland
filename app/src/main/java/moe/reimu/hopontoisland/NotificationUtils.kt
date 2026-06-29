@file:OptIn(ExperimentalAtomicApi::class)

package moe.reimu.hopontoisland

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.merge
import moe.reimu.hopontoisland.ui.theme.DarkColorScheme
import moe.reimu.hopontoisland.ui.theme.LightColorScheme
import org.json.JSONObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

const val defaultChannelId = "default"
const val liveUpdateChannelId = "liveUpdate"

val currentId = AtomicInt(100)

@SuppressLint("MissingPermission")
fun postLiveUpdate(
    context: Context,
    critText: String,
    title: String,
    content: String,
    @DrawableRes icon: Int,
    @ColorInt color: Int,
) {
    val notifManager = NotificationManagerCompat.from(context)
    val settings = MyApplication.getInstance().getSettings()

    val builder = NotificationCompat.Builder(context, liveUpdateChannelId)
        .setSmallIcon(icon)
        .setOngoing(true)
        .setContentTitle(mergeCritTitle(critText, title))
        .setContentText(content)
        .setColor(color)
        .setContentIntent(
            PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).setStyle(
            NotificationCompat.BigTextStyle().bigText(content)
        )

    when (settings.notificationMethod) {
        NotificationMethod.NATIVE_LIVE_UPDATES -> {
            setupNativeLiveUpdate(builder, critText)
        }

        NotificationMethod.HYPEROS_DYNAMIC_ISLANDS -> {
            setupHyperOSLiveUpdate(context, builder, title, critText, content, icon, color)
        }
    }

    notifManager.notify(123, builder.build())
}

fun setupNativeLiveUpdate(builder: NotificationCompat.Builder, critText: String) {
    builder
        .setShortCriticalText(critText)
        .setRequestPromotedOngoing(true)
}

fun setupHyperOSLiveUpdate(
    context: Context,
    builder: NotificationCompat.Builder,
    title: String,
    critText: String,
    content: String,
    @DrawableRes icon: Int,
    @ColorInt color: Int,
) {
    val islandParams = JSONObject()
    val paramV2 = JSONObject().also { islandParams.put("param_v2", it) }
    paramV2.put("protocol", 1)
    paramV2.put("business", "dining")
    paramV2.put("enableFloat", false)
    paramV2.put("updatable", true)
    paramV2.put("reopen", "reopen")
    paramV2.put("param_island", JSONObject().apply {
        put("highlightColor", String.format("#%06X", 0xFFFFFF.and(color)))
        put("bigIslandArea", JSONObject().apply {
            put("imageTextInfoLeft", JSONObject().apply {
                put("type", 1)
                put("textInfo", JSONObject().apply {
                    put("title", title)
                    put("showHighlightColor", true)
                })
                put("picInfo", JSONObject().apply {
                    put("type", 1)
                    put("pic", "miui.focus.pic_icon")
                })
            })
            put("textInfo", JSONObject().apply {
                put("title", critText)
                put("showHighlightColor", true)
            })
        })
    })
    paramV2.put("baseInfo", JSONObject().apply {
        put("type", 2)
        put("title", mergeCritTitle(critText, title))
        put("content", content)
    })

    val pics = Bundle().apply {
        putParcelable("miui.focus.pic_icon", Icon.createWithResource(context, icon).setTint(color))
    }

    // Finish up
    val extras = Bundle()
    extras.putString("miui.focus.param", islandParams.toString())
    extras.putBundle("miui.focus.pics", pics)
    builder.addExtras(extras)
}

fun mergeCritTitle(critText: String, title: String): String {
    if (title.isBlank()) return critText
    return "$critText | $title"
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
        context.getString(R.string.error),
        context.getString(R.string.noti_error),
        content,
        R.drawable.ic_warning,
        Color.valueOf(errorColor.red, errorColor.green, errorColor.blue).toArgb()
    )
}