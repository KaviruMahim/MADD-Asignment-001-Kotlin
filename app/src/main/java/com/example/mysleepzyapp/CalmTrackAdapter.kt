package com.example.mysleepzyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CalmTrackAdapter(
    private val items: List<CalmMusicFragment.Track>,
    private val onClick: (CalmMusicFragment.Track) -> Unit
) : RecyclerView.Adapter<CalmTrackAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.imgCover)
        val title: TextView = view.findViewById(R.id.txtTitle)
        val artist: TextView = view.findViewById(R.id.txtArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track_card, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.artist.text = item.artist
        Glide.with(holder.cover.context)
            .load(item.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(holder.cover)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
