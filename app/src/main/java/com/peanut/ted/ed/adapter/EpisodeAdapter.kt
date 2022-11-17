package com.peanut.ted.ed.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.data.Episode
import com.peanut.ted.ed.utils.Unities.copy
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.utils.Unities.play
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.utils.Unities.round
import com.peanut.ted.ed.utils.Unities.toast
import com.peanut.ted.ed.viewholder.EpisodeViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso

class EpisodeAdapter(
        private val context: Context,
        private val dataset: MutableList<Episode>,
        private val album: String
) : RecyclerView.Adapter<EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EpisodeViewHolder(LayoutInflater.from(context).inflate(R.layout.episode_layout, parent, false))

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.episodeName.text = dataset[position].episodeName
        holder.episodeCategory.text = dataset[position].desc
        holder.timeLasts.text = dataset[position].timeLasts
        holder.keyInfo.text = dataset[position].keyInfo
        holder.episodePreview.round(12.dp)
        loadImg(dataset[position].previewUrl, holder.episodePreview)
        holder.actionPlay.setOnClickListener {
            ViewModel.watchingPosition = position to System.currentTimeMillis()
            dataset[position].getRawLink(album).play(this@EpisodeAdapter.context)
        }
        holder.actionLink.setOnClickListener {
            val link = dataset[position].getRawLink(album)
            link.copy(this@EpisodeAdapter.context)
            "已复制链接".toast(this.context)
            context.startActivity(Intent(Intent.ACTION_VIEW).apply { this.setDataAndType(Uri.parse(link), "video/mp4") })
        }
        holder.actionBook.setOnClickListener {
            autoBookmark(position)
        }
    }

    private inline val Int.dp: Float
        get() = run {
            return toFloat().dp
        }

    private inline val Float.dp: Float
        get() = run {
            val scale: Float = context.resources.displayMetrics.density
            return (this * scale + 0.5f)
        }

    fun autoBookmark(position: Int){
        "${ViewModel.ServerIp.resolveUrl()}/toggleBookmark?path=${Uri.encode("/"+album+"/"+dataset[position].episodeName)}".http(this@EpisodeAdapter.context) {
            dataset.removeAt(position)
            notifyItemRemoved(position)
            if (position != itemCount) {
                notifyItemRangeChanged(position, itemCount)
            }
        }
    }

    fun getDataAtPosition(position: Int) = dataset[position]

    override fun getItemCount(): Int {
        return dataset.size
    }

    private fun loadImg(url: String,iv: ImageView) {
        Picasso.get().load(url).priority(Picasso.Priority.HIGH).error(R.mipmap.preview)
            .placeholder(R.mipmap.preview).fit().into(iv)
    }
}