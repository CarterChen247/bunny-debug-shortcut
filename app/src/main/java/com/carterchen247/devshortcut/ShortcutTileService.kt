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

        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        startActivityAndCollapseInternal(intent)
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startActivityAndCollapseInternal(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivityAndCollapse(intent)
    }
}