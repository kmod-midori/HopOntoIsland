package moe.reimu.hopontoisland

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ScanTileService : TileService() {
    override fun onClick() {
        super.onClick()

        // 獲取方塊狀態
        if (qsTile == null) return

        // 用戶點擊時，發送廣播給我們的無障礙服務
        val intent = Intent(ACTION_TRIGGER_SCREENSHOT)
        // 設置包名，確保廣播只發送給自己的 App，提高安全性
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        // 方塊可見時更新狀態，確保它是活躍狀態
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    companion object {
        const val ACTION_TRIGGER_SCREENSHOT: String =
            "moe.reimu.hopontoisland.ACTION_TRIGGER_SCREENSHOT"
        private const val TAG = "ScanTileService"
    }
}