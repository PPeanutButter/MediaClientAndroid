package com.peanut.ted.ed.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.encodeBased64
import com.peanut.ted.ed.utils.Unities.name
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.viewmodel.ViewModel
import com.peanut.ted.ed.viewholder.AttachViewHolder

class AttachAdapter(
        private val context: Context,
        private val titles: MutableList<String>
) : RecyclerView.Adapter<AttachViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AttachViewHolder(LayoutInflater.from(context).inflate(R.layout.attach_layout, parent, false))

    override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
        holder.attachName.text = titles[position].name()
        val server = ViewModel.ServerIp.resolveUrl()
        holder.attachDownload.setOnClickListener {
            val link = "$server/getFile2/${titles[position].name()}" +
                    "?path=${("/"+titles[position]).encodeBased64()}" +
                    "&token=${SettingManager.getValue("token", "")}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(link), "video/mp4")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}