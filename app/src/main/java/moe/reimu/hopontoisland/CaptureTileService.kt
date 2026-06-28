package moe.reimu.hopontoisland

import android.app.PendingIntent
import android.service.quicksettings.TileService
import kotlin.time.Duration.Companion.milliseconds

class CaptureTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = CaptureActivity.getCaptureIntent(this, 500.milliseconds)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startActivityAndCollapse(pendingIntent)
    }
}
