package com.peanut.ted.ed.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.Preference
import com.peanut.sdk.miuidialog.MIUIDialog
import com.peanut.ted.ed.data.HDR

class HDRPreference: Preference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttached() {
        var hdrType: IntArray = intArrayOf(-1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             context.display?.hdrCapabilities?.supportedHdrTypes?.let {
                 hdrType = it
             }
        }
        val hdr = Array(hdrType.size){""}
        hdrType.forEachIndexed { index, i ->
            hdr[index] = HDR.fromType(i).desc
        }
        this.summary = hdr.joinToString(separator = System.lineSeparator())
        super.onAttached()
    }
}