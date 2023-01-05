package com.peanut.ted.ed.view

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.provider.MediaStore.Audio.Media
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import com.peanut.sdk.miuidialog.MIUIDialog
import com.peanut.ted.ed.data.HDR

class CodecPreference: Preference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val names = Array(3){ mutableListOf<String>() }
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.forEach {
                if (!it.isEncoder && it.isHardwareAccelerated){
                    println(it.name+it.supportedTypes.contentToString())
                    for (mime in it.supportedTypes){
                        when(mime){
                            MediaFormat.MIMETYPE_VIDEO_VP9 -> {
                                names[0].add(it.name)
                            }
                            MediaFormat.MIMETYPE_VIDEO_HEVC -> {
                                names[1].add(it.name)
                            }
                            MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION -> {
                                names[2].add(it.name)
                            }
                        }
                    }
                }
            }
            val values = listOf(
                "vp9" + if (isSecure(names[0])) "（安全）" else "",
                "hevc" + if (isSecure(names[1])) "（安全）" else "",
                "dolby-vision" + if (isSecure(names[2])) "（安全）" else "",
            )
            this.summary = values.joinToString(separator = System.lineSeparator())
        }
        super.onAttached()
    }

    fun isSecure(mutableList: MutableList<String>):Boolean{
        for (a in mutableList){
            if (a.indexOf("secure") != -1) return true
        }
        return false
    }
}