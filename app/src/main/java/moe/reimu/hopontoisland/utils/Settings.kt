package moe.reimu.hopontoisland.utils

import android.content.Context
import androidx.core.content.edit
import moe.reimu.hopontoisland.CaptureMethod
import moe.reimu.hopontoisland.NotificationMethod

class Settings(context: Context): ISettings {
    private val sp = context.getSharedPreferences("default", Context.MODE_PRIVATE)

    override var modelProvider: String
        get() = sp.getString("modelProvider", "openai")!!
        set(value) {
            sp.edit {
                putString("modelProvider", value)
            }
        }

    override var modelUrl: String?
        get() = sp.getString("modelUrl", null)
        set(value) {
            sp.edit {
                putString("modelUrl", value)
            }
        }


    override var modelKey: String?
        get() = sp.getString("modelKey", null)
        set(value) {
            sp.edit {
                putString("modelKey", value)
            }
        }

    override var modelName: String?
        get() = sp.getString("modelName", null)
        set(value) {
            sp.edit {
                putString("modelName", value)
            }
        }

    override var captureMethod: CaptureMethod
        get() = CaptureMethod.fromKey(sp.getString("captureMethod", null) ?: CaptureMethod.DEFAULT.key)
        set(value) {
            sp.edit {
                putString("captureMethod", value.key)
            }
        }

    override var notificationMethod: NotificationMethod
        get() = NotificationMethod.fromKey(sp.getString("notificationMethod", null) ?: NotificationMethod.DEFAULT.key)
        set(value) {
            sp.edit {
                putString("notificationMethod", value.key)
            }
        }
}