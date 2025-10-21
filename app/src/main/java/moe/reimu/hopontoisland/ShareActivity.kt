package moe.reimu.hopontoisland

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.graphics.toColorInt
import java.lang.Exception

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                try {
                    postLiveUpdate(
                        this, sharedText, sharedText, "",
                        R.drawable.ic_info,
                        "#3DDC84".toColorInt()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to post notification", e)
                }
            }
        }

        finish()
    }

    companion object {
        private const val TAG = "ShareActivity"
    }
}