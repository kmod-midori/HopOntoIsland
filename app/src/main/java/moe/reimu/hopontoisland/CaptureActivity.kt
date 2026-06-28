package moe.reimu.hopontoisland

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val serviceInstance = TextAccessibilityService.getInstance()
        if (serviceInstance == null) {
            Toast.makeText(this, "Accessibility service not running", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val delayMs = intent.getLongExtra(KEY_DELAY_MS, 0).coerceAtLeast(0)
        serviceInstance.startCapture(delayMs.milliseconds)

        finish()
    }

    companion object {
        const val KEY_DELAY_MS = "delayMs"

        fun getCaptureIntent(context: Context, delay: Duration = 500.milliseconds) =
            Intent(context, CaptureActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(KEY_DELAY_MS, delay.inWholeMilliseconds)
            }
    }
}