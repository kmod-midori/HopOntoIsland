package moe.reimu.hopontoisland

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@SuppressLint("AccessibilityPolicy")
class TextAccessibilityService : AccessibilityService() {
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var tileReceiver: BroadcastReceiver? = null

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

        // 注册广播接收器，监听 Tile 点击
        tileReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (ACTION_TRIGGER_SCREENSHOT == intent.action) { // 这里的 Action 字符串要和 TileService 里一致
                    Log.d("ScreenReader", "收到指令，准备读取屏幕节点...")
                    // 收起控制中心面板
                    // 如果不收起，可能会读到控制中心的内容，而不是下面的 App
                    val closed = performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                    // 延迟执行 (关键！)
                    // 面板收起需要动画时间，建议给 500ms，等待下方 App 完全变成 Active Window
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        (GlobalScope + Dispatchers.IO).launch {
                            try {
                                UICore.doCapture(this@TextAccessibilityService)
                            } catch (e: Exception) {
                                Log.e(TAG, "doCapture failed", e)
                                UICore.postErrorNotification(this@TextAccessibilityService, e.message.orEmpty())
                            }
                        }
                    }, (if (closed) 500 else 100).toLong())
                }
            }
        }
        // 这里的 Action 必须和 ScanTileService 里定义的一模一样
        registerReceiver(
            tileReceiver,
            IntentFilter(ACTION_TRIGGER_SCREENSHOT),
            RECEIVER_EXPORTED
        )

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
        const val ACTION_TRIGGER_SCREENSHOT: String =
            "moe.reimu.hopontoisland.ACTION_TRIGGER_SCREENSHOT"
        private const val TAG = "TextAccessibilityService"
        private var instance: TextAccessibilityService? = null
        fun getInstance(): TextAccessibilityService? {
            return instance
        }
    }
}