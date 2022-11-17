package com.peanut.ted.ed.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.activity.EpisodeActivity
import com.peanut.ted.ed.data.Album
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.utils.Unities.round
import com.peanut.ted.ed.viewholder.AlbumViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import java.util.regex.Pattern

/**
 * 显示海报墙
 */
class AlbumAdapter(
        private val context: Context,
        private val activity: Activity,
        private var albums: MutableList<Album>
) : RecyclerView.Adapter<AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AlbumViewHolder(LayoutInflater.from(context).inflate(R.layout.album_layout, parent, false))

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        Pattern.compile("(.*)\\(\\d{4}\\)", Pattern.MULTILINE).matcher(albums[position].albumTitle).apply { this.find() }.also {
            if (it.groupCount() == 1)
                holder.albumName.text = it.group(1)
            else holder.albumName.text = albums[position].albumTitle
        }
        val server = ViewModel.ServerIp.resolveUrl()
        loadImg("$server/getCover?cover=${Uri.encode(albums[position].albumPath)}&" +
                "token=${SettingManager.getValue("token", "")}", holder.albumPreview)
        holder.albumRoot.setOnClickListener {
            ViewModel.MainActivity2DetailActivityImage = holder.albumPreview.drawable
            context.startActivity(Intent(context, EpisodeActivity::class.java).putExtra("ALBUM", albums[position].albumPath), ActivityOptions.makeSceneTransitionAnimation(
                activity, holder.albumPreview, "ted-cover").toBundle())
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    private fun loadImg(url: String,iv: ImageView) {
        Picasso.get().load(url).priority(Picasso.Priority.HIGH).error(R.mipmap.cover)
            .placeholder(R.mipmap.cover).fit().memoryPolicy(MemoryPolicy.NO_CACHE).into(iv)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeDataset(albums: MutableList<Album>){
        this.albums = albums
        this.notifyDataSetChanged()
    }
}