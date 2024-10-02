package com.carterchen247.devshortcut

import android.service.quicksettings.TileService
import android.util.Log

class ShortcutTileService: TileService() {

    override fun onClick() {
        super.onClick()
        Log.d("ShortcutTileService", "onClick")
    }
}