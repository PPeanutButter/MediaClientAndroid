package com.peanut.ted.ed.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.data.Episode
import com.peanut.ted.ed.data.PlayHistory
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.download
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.utils.Unities.play
import com.peanut.ted.ed.viewholder.EpisodeViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

class EpisodeAdapter(
    private val context: Context,
    private val dataset: MutableList<Episode>,
    private val album: String,
    private val title: String
) : RecyclerView.Adapter<EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(
            LayoutInflater.from(context).inflate(R.layout.view_card_episode, parent, false)
        )

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.episodeName.text = dataset[position].episodeName
        holder.episodeCategory.text = dataset[position].desc
        holder.timeLasts.text = dataset[position].timeLasts
        holder.keyInfo.text = dataset[position].keyInfo
        holder.actionPlay.setOnClickListener {
            ViewModel.watchingPosition = position to System.currentTimeMillis()
            dataset[position].getRawLink(album, title).play(this@EpisodeAdapter.context)
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    SettingManager.savePlayHistory(
                        PlayHistory.fromEpisode(dataset[position], album, title)
                    )
                }
            }
        }
        holder.actionLink.setOnClickListener {
            dataset[position].getRawLink(album).download(context)
        }
        holder.actionBook.setOnClickListener {
            autoBookmark(position)
        }
        Picasso.get().load(dataset[position].previewUrl)
            .priority(Picasso.Priority.HIGH)
            .error(R.mipmap.preview)
            .placeholder(R.mipmap.preview)
            .fit()
            .into(holder.episodePreview)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun autoBookmark(position: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                "${SettingManager.getIp()}/toggleBookmark?path=${Uri.encode("/" + album + "/" + dataset[position].episodeName)}".http()
                val playHistory = SettingManager.readPlayHistory()
                //如果标记的是上次正在看的，那就取消，否则不管
                if (playHistory == PlayHistory.fromEpisode(dataset[position], album, title))
                    SettingManager.savePlayHistory(PlayHistory.Empty)
            }
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
}