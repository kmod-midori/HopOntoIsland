package moe.reimu.hopontoisland.utils

import android.content.Context
import androidx.core.content.edit

class Settings(context: Context) {
    private val sp = context.getSharedPreferences("default", Context.MODE_PRIVATE)

    var modelUrl: String?
        get() = sp.getString("modelUrl", null)
        set(value) {
            sp.edit {
                putString("modelUrl", value)
            }
        }

    var modelKey: String?
        get() = sp.getString("modelKey", null)
        set(value) {
            sp.edit {
                putString("modelKey", value)
            }
        }

    var modelName: String?
        get() = sp.getString("modelName", null)
        set(value) {
            sp.edit {
                putString("modelName", value)
            }
        }
}