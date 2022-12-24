package com.peanut.ted.ed.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.peanut.sdk.petlin.Extend.encodeBase64
import com.peanut.sdk.petlin.Extend.getFileName
import com.peanut.ted.ed.R
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.viewholder.AttachViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel

class AttachAdapter(
        private val context: Context,
        private val titles: MutableList<String>
) : RecyclerView.Adapter<AttachViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AttachViewHolder(LayoutInflater.from(context).inflate(R.layout.attach_layout, parent, false))

    override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
        holder.attachName.text = titles[position].getFileName()
        holder.attachDownload.setOnClickListener {
            val link = "${SettingManager.getIp()}/getFile2/${titles[position].getFileName()}" +
                    "?path=${("/"+titles[position]).encodeBase64(Base64.NO_WRAP, Base64.URL_SAFE)}" +
                    "&token=${ViewModel.token}"
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(link), "application/x-download")
                // adb shell "dumpsys window | grep mCurrentFocus"
                intent.setClassName(
                    "com.android.providers.downloads.ui",
                    "com.android.providers.downloads.ui.activity.BrowserDownloadActivity"
                )
                context.startActivity(intent)
            }catch (e:Exception){
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(link), "application/x-download")
                    // in case not working on some system
                    context.startActivity(intent)
                }catch (ae:Exception){
                    ae.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}