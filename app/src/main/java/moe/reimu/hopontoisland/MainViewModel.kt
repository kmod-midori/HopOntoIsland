package moe.reimu.hopontoisland

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.reimu.hopontoisland.utils.ISettings
import moe.reimu.hopontoisland.utils.PreviewSettings

open class MainViewModel(
    settings: ISettings = MyApplication.getInstance().getSettings(),
) : ViewModel() {

    // --- Model settings (editable, backed by SharedPreferences) ---
    private val _modelProvider = MutableStateFlow(settings.modelProvider)
    val modelProvider: StateFlow<String> = _modelProvider.asStateFlow()

    private val _modelUrl = MutableStateFlow(settings.modelUrl.orEmpty())
    val modelUrl: StateFlow<String> = _modelUrl.asStateFlow()

    private val _modelKey = MutableStateFlow(settings.modelKey.orEmpty())
    val modelKey: StateFlow<String> = _modelKey.asStateFlow()

    private val _modelName = MutableStateFlow(settings.modelName.orEmpty())
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    private val _captureMethod = MutableStateFlow(settings.captureMethod)
    val captureMethod: StateFlow<CaptureMethod> = _captureMethod.asStateFlow()

    // --- Runtime system status (checked on each resume) ---
    private val _canPostPromotedNotifications = MutableStateFlow(false)
    val canPostPromotedNotifications: StateFlow<Boolean> =
        _canPostPromotedNotifications.asStateFlow()

    private val _serviceEnabled = MutableStateFlow(false)
    val serviceEnabled: StateFlow<Boolean> = _serviceEnabled.asStateFlow()

    // --- One-shot snackbar events ---
    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    // --- Actions ---

    fun updateModelProvider(provider: String) {
        _modelProvider.value = provider
    }

    fun updateCaptureMethod(method: CaptureMethod) {
        _captureMethod.value = method
    }

    open fun refreshNotificationCapability() {
        val notifManager = NotificationManagerCompat.from(MyApplication.getInstance())
        _canPostPromotedNotifications.value = notifManager.canPostPromotedNotifications()
    }

    open fun refreshServiceStatus() {
        _serviceEnabled.value = TextAccessibilityService.getInstance() != null
    }

    fun saveSettings(url: String, key: String, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val s = MyApplication.getInstance().getSettings()
                s.modelProvider = _modelProvider.value
                s.modelUrl = url
                s.modelKey = key
                s.modelName = name
                s.captureMethod = _captureMethod.value
            }
            _snackbarEvents.emit(
                MyApplication.getInstance().getString(R.string.saved_msg)
            )
        }
    }
}

/**
 * ViewModel variant for use in Compose previews.
 * Uses [PreviewSettings] so no real [MyApplication] instance is needed.
 * Overrides runtime-status methods as no-ops since there is no real system to query.
 */
class PreviewMainViewModel : MainViewModel(PreviewSettings) {
    override fun refreshNotificationCapability() {}
    override fun refreshServiceStatus() {}
}
