package moe.reimu.hopontoisland

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class CaptureActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 必須延遲！否則你會截到正在收起的面板殘影
        Handler(Looper.getMainLooper()).postDelayed({
            (GlobalScope + Dispatchers.IO).launch {
                try {
                    UICore.doCapture(this@CaptureActivity)
                } catch (e: Exception) {
                    Log.e(TAG, "doCapture failed", e)
                    UICore.postErrorNotification(this@CaptureActivity, e.message.orEmpty())
                }
            }
        }, 500)

        finish()
    }

    companion object {
        private const val TAG = "CaptureActivity"
    }
}