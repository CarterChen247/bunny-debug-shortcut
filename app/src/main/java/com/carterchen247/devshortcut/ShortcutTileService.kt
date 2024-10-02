package com.carterchen247.devshortcut

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.Toast

class ShortcutTileService : TileService() {

    override fun onClick() {
        super.onClick()
        // check if development settings are enabled
        val enabled = Settings.Secure.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        if (enabled == 0) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivityAndCollapseInternal(intent)
            Toast.makeText(this, "Please enable development settings first", Toast.LENGTH_LONG).show()
            return
        }

        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityAndCollapseInternal(intent)
            return
        }

        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        startActivityAndCollapseInternal(intent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        if (accessibilityEnabled == 1) {
            val service = "${packageName}/${AccessibilityHelperService::class.java.canonicalName}"
            val settingValue = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return (settingValue?.contains(service) == true)
        }
        return false
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startActivityAndCollapseInternal(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivityAndCollapse(intent)
    }
}