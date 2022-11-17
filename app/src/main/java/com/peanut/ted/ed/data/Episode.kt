package com.peanut.ted.ed.data

import android.net.Uri
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.encodeBased64
import com.peanut.ted.ed.utils.Unities.name
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.utils.Unities.second2TimeDesc
import com.peanut.ted.ed.viewmodel.ViewModel

class Episode(episodePath: String,
              val timeSeconds: Double,
              val bitrate: String,
              val date: String) {

    val episodeName: String
    val previewUrl: String
    val timeLasts: String

    init {
        episodeName = episodePath.name()
        val server = ViewModel.ServerIp.resolveUrl()
        previewUrl = "$server/getVideoPreview?path=${Uri.encode(episodePath)}" +
                "&token=${SettingManager.getValue("token", "")}"
        timeLasts = timeSeconds.second2TimeDesc()
    }
    val desc get() = "$bitrate $date"

    val keyInfo get() = String.format("%s%s",
            if (episodeName.indexOf("2160p", 0, true) != -1) "4K " else "",
            if (episodeName.indexOf("hdr", 0, true) != -1) "HDR" else "")

    fun getRawLink(album: String):String{
        val server = ViewModel.ServerIp.resolveUrl()
        return "$server/getFile2/${episodeName}?" +
                "path=${("/$album/$episodeName").encodeBased64()}&" +
                "token=${SettingManager.getValue("token", "")}"
    }
}
