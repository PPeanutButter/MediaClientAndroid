package com.peanut.ted.ed.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.peanut.ted.ed.R
import com.peanut.ted.ed.activity.EpisodeActivity
import com.peanut.ted.ed.data.Album
import com.peanut.ted.ed.viewholder.AlbumViewHolder
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso

/**
 * 显示海报墙
 */
class AlbumAdapter(
    private val context: Context,
    private val activity: Activity,
    private var albums: MutableList<Album>
) : RecyclerView.Adapter<AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AlbumViewHolder(LayoutInflater.from(context).inflate(R.layout.view_card_album, parent, false))

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.albumName.text = albums[position].albumDisplayName
        holder.albumRoot.setOnClickListener {
            ViewModel.MainActivity2DetailActivityImage = holder.albumPreview.drawable
            context.startActivity(
                Intent(context, EpisodeActivity::class.java)
                    .putExtra("ALBUM", albums[position].albumPath)
                    .putExtra("TITLE", albums[position].albumDisplayName),
                ActivityOptions.makeSceneTransitionAnimation(
                    activity, holder.albumPreview, "ted-cover"
                ).toBundle()
            )
        }
        Picasso.get().load(albums[position].albumCoverUrl)
            .priority(Picasso.Priority.HIGH)
            .error(R.mipmap.cover)
            .placeholder(R.mipmap.cover)
            .fit().into(holder.albumPreview)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeDataset(albums: MutableList<Album>) {
        this.albums = albums
        this.notifyDataSetChanged()
    }
}