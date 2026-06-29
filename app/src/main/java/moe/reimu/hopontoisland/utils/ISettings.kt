package moe.reimu.hopontoisland.utils

import moe.reimu.hopontoisland.CaptureMethod

interface ISettings {
    var modelProvider: String
    var modelKey: String?
    var modelUrl: String?
    var modelName: String?
    var captureMethod: CaptureMethod
}