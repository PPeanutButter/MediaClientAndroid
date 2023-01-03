package com.peanut.ted.ed.adapter

import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.peanut.sdk.petlin.Extend.encodeBase64
import com.peanut.sdk.petlin.Extend.getFileName
import com.peanut.ted.ed.R
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.download
import com.peanut.ted.ed.viewholder.AttachViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel

class AttachAdapter(
        private val context: Context,
        private val titles: MutableList<String>
) : RecyclerView.Adapter<AttachViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AttachViewHolder(LayoutInflater.from(context).inflate(R.layout.view_card_attach, parent, false))

    override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
        holder.attachName.text = titles[position].getFileName()
        holder.attachDownload.setOnClickListener {
            val link = "${SettingManager.getIp()}/getFile2/${titles[position].getFileName()}" +
                    "?path=${("/"+titles[position]).encodeBase64(Base64.NO_WRAP, Base64.URL_SAFE)}" +
                    "&token=${ViewModel.token}"
            link.download(context)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}