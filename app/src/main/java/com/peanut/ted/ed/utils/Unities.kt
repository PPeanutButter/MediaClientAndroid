package com.peanut.ted.ed.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import com.peanut.sdk.okhttp3.CacheStoreCookieJar
import com.peanut.sdk.okhttp3.OnReceiveCookieCallback
import com.peanut.ted.ed.viewmodel.ViewModel
import okhttp3.*
import java.io.IOException

object Unities {

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


    fun String.resolveUrl() = if (this.startsWith("http") || this.startsWith("HTTP")) {
        this
    } else "http://$this"

    fun String.http(context: Context? = null, func: (String?) -> Unit) {
        val client = okHttpClient
        val request: Request = Request.Builder()
            .url(this)
            .build()
        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("onFailure")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("onResponse")
                println(call.request().url.encodedPath+":"+call.request().header("Cookie"))
                val s = response.body?.string()
                if (context != null) {
                    Handler(context.mainLooper).post {
                        func.invoke(s)
                    }
                }else{
                    func.invoke(s)
                }
            }
        })
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
