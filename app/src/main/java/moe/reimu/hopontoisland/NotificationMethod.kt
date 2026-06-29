package moe.reimu.hopontoisland

import android.annotation.SuppressLint

enum class NotificationMethod(
    val key: String,
    val displayNameRes: Int,
    val available: Boolean,
) {
    NATIVE_LIVE_UPDATES(
        "native_live_updates",
        R.string.notification_method_native_live_updates,
        true,
    ),
    HYPEROS_DYNAMIC_ISLANDS(
        "hyperos_dynamic_islands",
        R.string.notification_method_hyperos_dynamic_islands,
        isHyperOsAvailable()
    );

    companion object {
        val DEFAULT = NATIVE_LIVE_UPDATES
        fun fromKey(key: String) = entries.find { it.key == key } ?: DEFAULT
    }
}

@SuppressLint("PrivateApi")
fun isHyperOsAvailable(): Boolean {
    return try {
        val cls = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
        method.invoke(null, "persist.sys.feature.island", false) as Boolean
    } catch (_: Exception) {
        false
    }
}
