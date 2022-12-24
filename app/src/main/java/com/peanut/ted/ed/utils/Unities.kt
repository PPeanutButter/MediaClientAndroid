package com.peanut.ted.ed.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.peanut.sdk.okhttp3.CacheStoreCookieJar
import com.peanut.sdk.okhttp3.OnReceiveCookieCallback
import com.peanut.ted.ed.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.regex.Pattern

object Unities {

    fun String.regex(regex: String, mode: Int, onRegex:(MutableList<String>)->String):String{
        val p = Pattern.compile(regex, mode).matcher(this)
        if (p.find()){
            val result = mutableListOf<String>()
            for (i in 1..p.groupCount()){
                p.group(i)?.let { result.add(it) }
            }
            return onRegex(result)
        }
        return ""
    }

    fun String.play(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(this), "video/mp4")
            intent.setClassName(
                "com.mxtech.videoplayer.pro",
                "com.mxtech.videoplayer.pro.ActivityScreen"
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "打开播放器失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun String.download(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(this), "application/x-download")
            // adb shell "dumpsys window | grep mCurrentFocus"
            intent.setClassName(
                "com.android.providers.downloads.ui",
                "com.android.providers.downloads.ui.activity.BrowserDownloadActivity"
            )
            context.startActivity(intent)
        }catch (e:Exception){
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(this), "application/x-download")
                // in case not working on some system
                context.startActivity(intent)
            }catch (ae:Exception){
                Toast.makeText(context, "提交下载任务失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun String.resolveUrl() = if (this.startsWith("http") || this.startsWith("HTTP")) {
        this
    } else "http://$this"

    suspend fun String.http(): String? {
        val body = withContext(Dispatchers.IO){
            val client = okHttpClient
            val request: Request = Request.Builder()
                .url(this@http)
                .build()
            client?.newCall(request)?.execute()
        }
        return body?.body?.string()
    }

    private var okHttpClient: OkHttpClient? = null

    fun getHttpClient(context: Context): OkHttpClient {
        if (okHttpClient == null) {
            synchronized("okHttpClient") {
                if (okHttpClient == null) {
                    okHttpClient = OkHttpClient.Builder()
                        .cookieJar(CacheStoreCookieJar(context, object : OnReceiveCookieCallback {
                            override fun onReceive(cookie: Cookie) {
                                if (cookie.name == "token") {
                                    ViewModel.token = cookie.value
                                }
                            }
                        }))
                        .build()
                }
            }
        }
        return okHttpClient!!
    }

}
