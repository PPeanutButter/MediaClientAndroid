package com.peanut.ted.ed.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.utils.Unities.round

class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val episodeCategory: TextView = itemView.findViewById(R.id.episode_category)
    val episodePreview: ImageView = itemView.findViewById(R.id.episode_preview)
    val infoBackground: ImageView = itemView.findViewById(R.id.info_background)
    val episodeName: TextView = itemView.findViewById(R.id.episode_name)
    val actionPlay: ImageView = itemView.findViewById(R.id.action_play)
    val actionLink: ImageView = itemView.findViewById(R.id.action_link)
    val actionBook: ImageView = itemView.findViewById(R.id.action_book)
    val timeLasts: TextView = itemView.findViewById(R.id.time_lasts)
    val keyInfo: TextView = itemView.findViewById(R.id.key_info)

}