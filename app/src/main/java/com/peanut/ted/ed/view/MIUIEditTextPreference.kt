package com.peanut.ted.ed.view

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.peanut.sdk.miuidialog.MIUIDialog

class MIUIEditTextPreference: EditTextPreference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        MIUIDialog(context).show {
            title(text = this@MIUIEditTextPreference.title.toString())
            input(prefill = this@MIUIEditTextPreference.text, waitForPositiveButton = false) { charSequence, _ ->
                this@MIUIEditTextPreference.text = charSequence.toString()
            }
            negativeButton(text = "Cancel")
            positiveButton(text = "OK")
        }
    }
}