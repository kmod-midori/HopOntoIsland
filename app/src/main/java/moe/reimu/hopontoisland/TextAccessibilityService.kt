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
        if (currentRoot == null) {
            return emptyList()
        }
        val textItems = dumpText(currentRoot)
            .sortedWith(compareBy({ it.second.top }, { it.second.left }))
        return textItems.map { it.first }
    }

    companion object {
        private const val TAG = "TextAccessibilityService"
        private var instance: TextAccessibilityService? = null
        fun getInstance(): TextAccessibilityService? {
            return instance
        }
    }
}