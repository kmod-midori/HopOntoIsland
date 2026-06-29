package moe.reimu.hopontoisland

enum class CaptureMethod(
    val key: String,
    val displayNameRes: Int,
    val requiresAccessibility: Boolean = false,
) {
    ACCESSIBILITY_TEXT(
        "accessibility_text", R.string.capture_method_accessibility_text,
        requiresAccessibility = true,
    ),
    ACCESSIBILITY_IMAGE(
        "accessibility_image", R.string.capture_method_accessibility_image,
        requiresAccessibility = true,
    ),
    ACCESSIBILITY_TEXT_IMAGE(
        "accessibility_text_image", R.string.capture_method_accessibility_text_image,
        requiresAccessibility = true,
    ),
    MEDIA_PROJECTION_IMAGE(
        "media_projection_image", R.string.capture_method_media_projection_image,
    ),
    SHIZUKU_IMAGE("shizuku_image", R.string.capture_method_shizuku_image),
    ROOT_IMAGE("root_image", R.string.capture_method_root_image);

    companion object {
        val DEFAULT = ACCESSIBILITY_TEXT
        fun fromKey(key: String): CaptureMethod =
            entries.find { it.key == key } ?: DEFAULT
    }
}
