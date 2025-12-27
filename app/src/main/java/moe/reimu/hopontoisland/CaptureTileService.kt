package moe.reimu.hopontoisland

import android.service.quicksettings.TileService
import kotlin.time.Duration.Companion.milliseconds

class CaptureTileService: TileService() {
    override fun onClick() {
        super.onClick()
        val accService = TextAccessibilityService.getInstance() ?: return
        accService.closeNotificationPanel()
        accService.startCapture(500.milliseconds)
    }
}