package moe.reimu.hopontoisland.utils

import moe.reimu.hopontoisland.CaptureMethod
import moe.reimu.hopontoisland.NotificationMethod

interface ISettings {
    var modelProvider: String
    var modelKey: String?
    var modelUrl: String?
    var modelName: String?
    var captureMethod: CaptureMethod
    var notificationMethod: NotificationMethod
}