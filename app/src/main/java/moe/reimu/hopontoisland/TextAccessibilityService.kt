package moe.reimu.hopontoisland

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.time.Duration

@SuppressLint("AccessibilityPolicy")
class TextAccessibilityService : AccessibilityService() {
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

    }

    fun dumpText(node: AccessibilityNodeInfo): List<Pair<String, Rect>> {
        val ret = mutableListOf<Pair<String, Rect>>()

        var nodeText = node.contentDescription
        if (nodeText.isNullOrBlank()) {
            nodeText = node.text
        }
        if (!nodeText.isNullOrBlank() && !isIconFont(nodeText)) {
            val pos = Rect()
            node.getBoundsInScreen(pos)
            ret.add(Pair(nodeText.toString(), pos))
        }

        for (i in 0 until node.childCount) {
            val child =
                node.getChild(i, AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_DEPTH_FIRST)
            if (child != null) {
                ret.addAll(dumpText(child))
            }
        }

        return ret
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected()")
    }

    override fun onCreate() {
        super.onCreate()
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val dumpedText = captureActiveWindowText().joinToString(" ")
                    Log.i(TAG, "text = [$dumpedText]")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to dump text", e)
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            IntentFilter("moe.reimu.hopontoisland.DUMP_TEXT"),
            ContextCompat.RECEIVER_EXPORTED
        )
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        instance = null
    }

    fun isIconFont(text: CharSequence): Boolean {
        for (ch in text) {
            val code = ch.code
            val inPUA = (code in 0xE000..0xF8FF)
            val inSuppPUA = (code in 0xF0000..0xFFFFD) || (code in 0x100000..0x10FFFD)
            if (!inPUA && !inSuppPUA) {
                return false // contains a normal character
            }
        }
        return true
    }

    fun captureActiveWindowText(): List<String> {
        val currentRoot =
            getRootInActiveWindow(AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_DEPTH_FIRST)
                ?: return emptyList()
        val textItems = dumpText(currentRoot)
            .sortedWith(compareBy({ it.second.top }, { it.second.left }))
        return textItems.map { it.first }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startCapture(delayDuration: Duration) {
        (GlobalScope + Dispatchers.IO).launch {
            delay(delayDuration)
            try {
                doCapture()
            } catch (e: Exception) {
                Log.e(TAG, "doCapture failed", e)
                postErrorNotification(
                    this@TextAccessibilityService, e.message.orEmpty()
                )
            }
        }
    }

    fun closeNotificationPanel(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
    }

    suspend fun doCapture() {
        postLiveUpdate(
            this,
            "识别中",
            "正在识别",
            "",
            R.drawable.ic_search,
            "#3DDC84".toColorInt(),
        )

        val dumpedText = try {
            captureActiveWindowText().joinToString(" ")
        } catch (e: Exception) {
            throw RuntimeException("Failed to obtain a11y text", e)
        }
        Log.i(TAG, "text = [$dumpedText]")

        val entity = RecognitionProcessor.recognizeText(dumpedText)
            ?: throw RuntimeException("Failed to recognize entity")

        val icon = when (entity.kind) {
            "dining" -> {
                R.drawable.ic_dining
            }

            "taxi" -> {
                R.drawable.ic_taxi
            }

            else -> {
                R.drawable.ic_info
            }
        }

        postLiveUpdate(
            this,
            entity.critText,
            "${entity.critText} | ${entity.title}",
            entity.content.orEmpty(),
            icon,
            "#3DDC84".toColorInt()
        )
    }

    companion object {
        private const val TAG = "TextAccessibilityService"
        private var instance: TextAccessibilityService? = null
        fun getInstance(): TextAccessibilityService? {
            return instance
        }
    }
}