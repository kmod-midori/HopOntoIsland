package moe.reimu.hopontoisland

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.toColorInt

const val defaultChannelId = "default"
const val liveUpdateChannelId = "liveUpdate"

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

    val builder = NotificationCompat.Builder(context, liveUpdateChannelId)
        .setSmallIcon(icon)
        .setOngoing(true)
        .setRequestPromotedOngoing(true)
        .setContentTitle(title)
        .setContentText(content)
        .setShortCriticalText(critText)
        .setColor(color)
        .setContentIntent(
            PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).setStyle(
            NotificationCompat.BigTextStyle().bigText(content)
        )
    notifManager.notify(123, builder.build())
}

fun clearLiveUpdate(context: Context) {
    val notifManager = NotificationManagerCompat.from(context)
    notifManager.cancel(123)
}