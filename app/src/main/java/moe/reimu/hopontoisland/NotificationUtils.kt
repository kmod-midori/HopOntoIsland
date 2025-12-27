package moe.reimu.hopontoisland

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import moe.reimu.hopontoisland.ui.theme.DarkColorScheme
import moe.reimu.hopontoisland.ui.theme.LightColorScheme

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