package moe.reimu.hopontoisland.utils

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
}