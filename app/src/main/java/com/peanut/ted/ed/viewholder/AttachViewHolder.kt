package com.peanut.ted.ed.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R

class AttachViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val attachName: TextView = itemView.findViewById(R.id.attach_name)
    val attachDownload: ImageView = itemView.findViewById(R.id.action_download)
}