package com.peanut.ted.ed.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.peanut.ted.ed.R

class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val albumPreview: ImageView = itemView.findViewById(R.id.album_preview)
    val albumName: TextView = itemView.findViewById(R.id.album_name)
    val albumRoot: MaterialCardView = itemView.findViewById(R.id.album_root)
}