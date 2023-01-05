package com.peanut.ted.ed.application

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities
import com.squareup.picasso.Downloader
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.PicassoProvider

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SettingManager.init(this)
        Unities.getHttpClient(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
        Picasso.setSingletonInstance(
            Picasso.Builder(this).downloader(OkHttp3Downloader(Unities.getHttpClient(this))).build()
        )
    }
}