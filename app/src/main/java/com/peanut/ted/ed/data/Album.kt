package com.peanut.ted.ed.data

import android.net.Uri
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.viewmodel.ViewModel
import java.util.regex.Pattern

data class Album(val albumTitle: String, val albumPath: String){
    var albumDisplayName: String
    val albumCoverUrl: String get() = "${SettingManager.getIp()}/getCover?" +
            "cover=${Uri.encode(this.albumPath)}"

    init {
        Pattern.compile("(.*)\\(\\d{4}\\)", Pattern.MULTILINE).matcher(this.albumTitle).apply { this.find() }.also {
            albumDisplayName =  if (it.groupCount() == 1)
                it.group(1)!!
            else this.albumTitle
        }
    }
}
