package com.peanut.ted.ed.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.peanut.sdk.okhttp3.CacheStoreCookieJar
import com.peanut.sdk.okhttp3.OnReceiveCookieCallback
import com.peanut.sdk.petlin.Extend.copy
import com.peanut.sdk.petlin.Extend.encodeBase64
import com.peanut.sdk.petlin.Extend.toast
import com.peanut.ted.ed.utils.FileCompat
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities
import com.peanut.ted.ed.viewmodel.ViewModel
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class ConvertAssActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            try {
                val (name, size) = FileCompat.getFileNameAndSize(this, it, false)
                Log.d("ConvertAssActivity", "onCreate: receive $name with $size")
                val dest = this.cacheDir.path + "/" + name
                FileCompat.copyFile(it, dest, this)
                val client = Unities.getHttpClient(this)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", "Square Logo")
                    .addFormDataPart("file", name, File(dest).asRequestBody())
                    .build()
                val request = Request.Builder()
                    .url(SettingManager.getIp() + "/a2s/uploadAss")
                    .post(requestBody)
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        e.localizedMessage?.toast(this@ConvertAssActivity)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            response.body?.string()?.let { r ->
                                r.toast(this@ConvertAssActivity)
                                JSONObject(r).let { res ->
                                    val file = res.getString("file")
                                    val code = res.getInt("code")
                                    if (code != -1) {
                                        val url =
                                            SettingManager.getIp() + "/a2s/downloadSrt?path=" +
                                                    file.encodeBase64(Base64.NO_WRAP, Base64.URL_SAFE) + "&token=" +
                                                    ViewModel.token
                                        try {
                                            url.copy(this@ConvertAssActivity)
                                            this@ConvertAssActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            e.localizedMessage?.toast(this@ConvertAssActivity)
                                        }
                                    }
                                }
                            }
                        }catch (e:Exception){
                            e.localizedMessage?.toast(this@ConvertAssActivity)
                        }
                    }
                })
            }catch (e:Exception){
                e.localizedMessage?.toast(this@ConvertAssActivity)
            }
        }
    }
}