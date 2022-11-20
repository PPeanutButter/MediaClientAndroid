package com.peanut.ted.ed.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Handler
import android.util.Base64
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Toast
import okhttp3.*
import kotlin.concurrent.thread

object Unities {
    fun String.copy(context: Context) {
        val clipboard: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("zhi", this)
        clipboard?.setPrimaryClip(clip)
    }
    fun Double.second2TimeDesc(): String{
        val temp = this.toInt()
        val HH = temp / 3600
        val mm = temp % 3600 / 60
        val ss = temp % 3600 % 60
        return when{
            HH > 0 -> "$HH:${"0".repeat(if (mm<10) 1 else 0)}$mm:${"0".repeat(if (ss<10) 1 else 0)}$ss"
            mm > 0 ->                                       "$mm:${"0".repeat(if (ss<10) 1 else 0)}$ss"
            else ->                                                                             "0:$ss"
        }
    }

    fun String.encodeBased64(): String = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)

    fun String.name() = this.substring(this.lastIndexOf("/") + 1)

    fun String.toast(context: Context, duration: Int = Toast.LENGTH_SHORT, delayMillis: Long = 0) =
        Handler(context.mainLooper).postDelayed({
            Toast.makeText(
                context, this,
                duration
            ).show()
        }, delayMillis)


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

    fun String.toast(context: Context){
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }

    fun String.resolveUrl() = if (this.startsWith("http") || this.startsWith("HTTP")) {
        this
    } else "http://$this"

    fun String.http(context: Context, func: (String?) -> Unit) {
        thread {
            val client = getHttpClient()
            val request: Request = Request.Builder()
                .url(this)
                .build()
            client.newCall(request).execute().use { response ->
                val s = response.body?.string()
                Handler(context.mainLooper).post {
                    func.invoke(s)
                }
            }
        }
    }

    fun String.httpThread(func: (String?) -> Unit) {
        val client = getHttpClient()
        val request: Request = Request.Builder()
            .url(this)
            .build()
        client.newCall(request).execute().use { response ->
            func.invoke(response.body?.string().also { println(it) })
        }
    }

    private var okHttpClient:OkHttpClient? = null

    fun getHttpClient():OkHttpClient{
        if (okHttpClient == null){
            synchronized("okHttpClient"){
                if (okHttpClient == null){
                    okHttpClient = OkHttpClient.Builder()
                        .cookieJar(object :CookieJar{
                            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                                return if(SettingManager.getValue("token", "") !="") listOf(
                                    Cookie.Builder().name("token")
                                        .value(SettingManager.getValue("token", ""))
                                        .domain(SettingManager.getValue("token_domain", ""))
                                        .build()
                                ) else emptyList()
                            }

                            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                                for (cookie in cookies) {
                                    if (cookie.name == "token")
                                        SettingManager["token"] = cookie.value
                                    SettingManager["token_domain"] = cookie.domain
                                }
                            }
                        })
                        .build()
                }
            }
        }
        return okHttpClient!!
    }

    fun getFileLengthDesc(length: Long): String {
        return when {
            length.shr(30) >= 1.0 -> String.format("%.2f", length / 1024.0 / 1024.0 / 1024.0) + "GB"
            length.shr(20) >= 1.0 -> String.format("%.2f", length / 1024.0 / 1024.0) + "MB"
            length.shr(10) >= 1.0 -> String.format("%.2f", length / 1024.0) + "KB"
            else -> String.format("%.2f", length / 1.0) + "B"
        }
    }

    fun View.round(radius:Float){
        val roundRectangle: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        this.outlineProvider = roundRectangle
        this.clipToOutline = true
    }

    fun calculateColorLightValue(argb: Int):Double{
        return try {
            val r = argb shr 16 and 0xff
            val g = argb shr 8 and 0xff
            val b = argb and 0xff
            (0.299 * r + 0.587 * g + 0.114 * b)/255.0
        }catch (e:Exception){
            e.printStackTrace()
            0.0
        }
    }

}
