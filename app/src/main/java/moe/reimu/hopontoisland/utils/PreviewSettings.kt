package moe.reimu.hopontoisland.utils

import moe.reimu.hopontoisland.CaptureMethod
import moe.reimu.hopontoisland.NotificationMethod

@Suppress("UNUSED_PARAMETER")
object PreviewSettings: ISettings {
    override var modelProvider: String
        get() = "openai"
        set(value) {}
    override var modelKey: String?
        get() = ""
        set(value) {}
    override var modelUrl: String?
        get() = ""
        set(value) {}
    override var modelName: String?
        get() = ""
        set(value) {}
    override var captureMethod: CaptureMethod
        get() = CaptureMethod.DEFAULT
        set(value) {}
    override var notificationMethod: NotificationMethod
        get() = NotificationMethod.DEFAULT
        set(value) {}
}