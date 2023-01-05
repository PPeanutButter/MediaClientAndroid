package com.peanut.ted.ed.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.Preference
import com.peanut.sdk.miuidialog.MIUIDialog
import com.peanut.ted.ed.data.HDR

class LuminancePreference: Preference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             context.display?.hdrCapabilities?.let {
                 val spec = listOf(
                     "Min: ${it.desiredMinLuminance} cd/m²",
                     "Max: ${it.desiredMaxLuminance} cd/m²",
                     "Avg: ${it.desiredMaxAverageLuminance} cd/m²")
                 this.summary = spec.joinToString(separator = System.lineSeparator())
             }
        }
        super.onAttached()
    }
}