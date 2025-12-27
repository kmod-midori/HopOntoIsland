package moe.reimu.hopontoisland.utils

import android.content.Context
import androidx.core.content.edit

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
}