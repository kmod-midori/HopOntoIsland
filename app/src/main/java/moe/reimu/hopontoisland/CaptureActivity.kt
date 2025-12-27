package moe.reimu.hopontoisland

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import kotlin.time.Duration.Companion.milliseconds

class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TextAccessibilityService.getInstance()?.startCapture(500.milliseconds)
        finish()
    }
}