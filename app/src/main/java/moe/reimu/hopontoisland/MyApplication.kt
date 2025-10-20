package moe.reimu.hopontoisland

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import moe.reimu.hopontoisland.utils.Settings

class MyApplication : Application() {
    val ktorClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()
        instance = this

        val notifManager = NotificationManagerCompat.from(this)
        val channels = listOf(
            NotificationChannelCompat.Builder(
                defaultChannelId,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            ).setName(getString(R.string.noti_channel_default)).build(),
            NotificationChannelCompat.Builder(
                liveUpdateChannelId,
                NotificationManagerCompat.IMPORTANCE_MAX
            ).setName(getString(R.string.noti_channel_live)).build()
        )
        notifManager.createNotificationChannelsCompat(channels)

        settings = Settings(this)
    }

    fun getSettings() = settings

    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }
}