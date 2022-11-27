package com.peanut.ted.ed.data

import android.net.Uri
import android.util.Base64
import com.peanut.sdk.petlin.Extend.describeAsTimeLasts
import com.peanut.sdk.petlin.Extend.encodeBase64
import com.peanut.sdk.petlin.Extend.getFileName
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.viewmodel.ViewModel

class Episode(episodePath: String,
              val timeSeconds: Double,
              private val bitrate: String,
              private val date: String) {

    val episodeName: String
    val previewUrl: String
    val timeLasts: String

    init {
        episodeName = episodePath.getFileName()
        previewUrl = "${SettingManager.getIp()}/getVideoPreview?path=${Uri.encode(episodePath)}" +
                "&token=${ViewModel.token}"
        timeLasts = timeSeconds.toInt().describeAsTimeLasts(hour = ":", minute = ":", seconds = "")
    }

    val desc get() = "$bitrate $date"

    val keyInfo get() = String.format("%s%s",
            if (episodeName.indexOf("2160p", 0, true) != -1) "4K " else "",
            if (episodeName.indexOf("hdr", 0, true) != -1) "HDR" else "")

    fun getRawLink(album: String):String{
        return "${SettingManager.getIp()}/getFile2/${episodeName}?" +
                "path=${("/$album/$episodeName").encodeBase64(Base64.NO_WRAP, Base64.URL_SAFE)}&" +
                "token=${ViewModel.token}"
    }
}
