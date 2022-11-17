package com.peanut.ted.ed.viewmodel

import android.graphics.drawable.Drawable
import com.peanut.ted.ed.utils.SettingManager

object ViewModel {
    var MainActivity2DetailActivityImage :Drawable? = null
    val ServerIp
        get() = SettingManager.getValue("ip", "192.168.211.208:80")
    var watchingPosition: Pair<Int, Long> = -1 to -1
}