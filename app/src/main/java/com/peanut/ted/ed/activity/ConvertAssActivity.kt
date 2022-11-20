package com.peanut.ted.ed.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.peanut.ted.ed.utils.FileCompat
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities
import com.peanut.ted.ed.utils.Unities.encodeBased64
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.utils.Unities.toast
import com.peanut.ted.ed.viewmodel.ViewModel
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File


class ConvertAssActivity : AppCompatActivity() {

    @Volatile
    private var isDownloading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            val (name, size) = FileCompat.getFileNameAndSize(this, it, false)
            Log.d("ConvertAssActivity", "onCreate: receive $name with $size")
            val dest = this.cacheDir.path + "/" + name
            FileCompat.copyFile(it, dest, this)

            val client = Unities.getHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "Square Logo")
                .addFormDataPart("file", name, File(dest).asRequestBody())
                .build()
            val request = Request.Builder()
                .url(ViewModel.ServerIp.resolveUrl() + "/uploadAss")
                .post(requestBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    e.localizedMessage?.toast(this@ConvertAssActivity)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { r ->
                        println(r)
                        JSONObject(r).let { res ->
                            val file = res.getString("file")
                            val code = res.getInt("code")
                            if (code != -1) {
                                val url = ViewModel.ServerIp.resolveUrl() + "/downloadSrt?path=" +
                                        file.encodeBased64() + "&token=" +
                                        SettingManager.getValue("token", "")
                                try {
                                    isDownloading = true
                                    this@ConvertAssActivity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                        this.setDataAndType(
                                            Uri.parse(url), "application/octet-stream"
                                        )
                                    })
                                } catch (e: Exception) {
                                    runOnUiThread { e.localizedMessage?.toast(this@ConvertAssActivity) }
                                } finally {
                                    this@ConvertAssActivity.finish()
                                }
                            }
                        }
                    }
                }
            })
        }
    }

}