package com.peanut.ted.ed.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import com.peanut.sdk.datastore.SettingsDatastore
import com.peanut.ted.ed.R
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.viewmodel.ViewModel

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        window.statusBarColor = Color.TRANSPARENT
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = SettingsDatastore(lifecycleScope, SettingManager.datastore!!)
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}