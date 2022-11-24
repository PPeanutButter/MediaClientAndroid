package com.peanut.ted.ed.application

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities

class MyApplication: Application() {
    
    override fun onCreate() {
        super.onCreate()
        SettingManager.init(this)
        Unities.getHttpClient(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}