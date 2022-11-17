package com.peanut.ted.ed.application

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.peanut.ted.ed.utils.SettingManager

class MyApplication: Application() {
    
    override fun onCreate() {
        super.onCreate()
        SettingManager.init(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}