package com.peanut.ted.ed.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.peanut.ted.ed.data.PlayHistory
import com.peanut.ted.ed.utils.Unities.resolveUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SettingManager {
    var datastore: DataStore<Preferences>? = null
    private val Context.datastore: DataStore<Preferences> by preferencesDataStore("settings")

    fun init(context: Context) {
        if (null == datastore)
            datastore = context.datastore
    }

    fun getUserName(): String {
        return runBlocking {
            datastore?.data?.map { p ->
                p[stringPreferencesKey("user")]
            }?.first() ?: ""
        }
    }

    fun getIp(): String {
        return runBlocking {
            datastore?.data?.map { p ->
                p[stringPreferencesKey("ip")]?.resolveUrl()
            }?.first() ?: ""
        }
    }

    fun getPassword(): String {
        return runBlocking {
            datastore?.data?.map { p ->
                p[stringPreferencesKey("password")]
            }?.first() ?: ""
        }
    }

    suspend fun getShow(): Boolean {
        return runBlocking {
            datastore?.data?.map { p ->
                p[booleanPreferencesKey("show_watched")]
            }?.first() ?: false
        }
    }

    suspend fun savePlayHistory(playHistory: PlayHistory){
        withContext(Dispatchers.IO){
            datastore?.edit {
                it[stringPreferencesKey("playHistoryId")] = playHistory.id
                it[stringPreferencesKey("playHistoryUrl")] = playHistory.url
            }
        }
    }

    suspend fun readPlayHistory(): PlayHistory{
        return withContext(Dispatchers.IO){
            datastore?.data?.map { p ->
                PlayHistory(
                    id = p[stringPreferencesKey("playHistoryId")]?:"",
                    url = p[stringPreferencesKey("playHistoryUrl")]?:""
                )
            }?.first()?: PlayHistory.Empty
        }
    }
}