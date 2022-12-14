package com.peanut.ted.ed.data

import android.net.Uri
import android.util.Base64
import com.peanut.sdk.petlin.Extend.describeAsTimeLasts
import com.peanut.sdk.petlin.Extend.encodeBase64
import com.peanut.sdk.petlin.Extend.getFileName
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.regex
import com.peanut.ted.ed.viewmodel.ViewModel
import java.util.regex.Pattern

class Episode(episodePath: String,
              val timeSeconds: Double,
              private val bitrate: String,
              private val date: String):Comparable<Episode> {

    val episodeName: String
    val previewUrl: String
    val timeLasts: String

    init {
        episodeName = episodePath.getFileName()
        previewUrl = "${SettingManager.getIp()}/getVideoPreview?path=${Uri.encode(episodePath)}"
        timeLasts = timeSeconds.toInt().describeAsTimeLasts(hour = ":", minute = ":", seconds = "")
    }

    val desc get() = "$bitrate $date"

    val keyInfo get() = String.format("%s%s",
            if (episodeName.indexOf("2160p", 0, true) != -1) "4K " else "",
            if (episodeName.indexOf("hdr", 0, true) != -1 || episodeName.indexOf("hlg", 0, true) != -1) "HDR" else "")

    fun getRawLink(album: String, title: String? = null):String{
        return "${SettingManager.getIp()}/getFile2/${if (title != null) getTitleDesc(title) else episodeName }?" +
                "path=${("/$album/$episodeName").encodeBase64(Base64.NO_WRAP, Base64.URL_SAFE)}&" +
                "token=${ViewModel.token}"
    }

    private fun getTitleDesc(title: String):String{
        val resolution = when{
            episodeName.indexOf("2160p", 0, true) != -1 -> "4K "
            episodeName.indexOf("1080p", 0, true) != -1 -> "1080P "
            else -> ""
        }
        val quality = when{
            episodeName.indexOf("hdr", 0, true) != -1 || episodeName.indexOf("hlg", 0, true) != -1 -> "HDR"
            episodeName.indexOf("bluray", 0, true) != -1 -> "蓝光"
            else -> ""
        }
        val episode = episodeName.regex("s(\\d+)e(\\d+)", Pattern.CASE_INSENSITIVE){
            String.format("第%s季第%s集 ",it[1],it[2])
        }
        return "$title $episode$resolution$quality"
    }

    private fun getSeasonEpisode():String{
        return episodeName.regex("s(\\d+)e(\\d+)", Pattern.CASE_INSENSITIVE, episodeName){
            it[0].uppercase()
        }
    }

    override fun compareTo(other: Episode): Int {
        return this.getSeasonEpisode().compareTo(other.getSeasonEpisode())
    }

}
