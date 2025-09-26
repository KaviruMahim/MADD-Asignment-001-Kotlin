package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalmMusicFragment : Fragment() {

    data class Track(
        val title: String,
        val artist: String,
        val imageUrl: String,
        val audioUrl: String
    )

    private val tracks = listOf(
        Track(
            title = "Ocean Waves",
            artist = "Calm Nature",
            imageUrl = "https://i.ytimg.com/vi/1ZYbU82GVz4/hq720.jpg?sqp=-oaymwEnCNAFEJQDSFryq4qpAxkIARUAAIhCGAHYAQHiAQoIGBACGAY4AUAB&rs=AOn4CLCSSSVpHdcuWpbY8PRDiBGhl-qamQ",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3"
        ),
        Track(
            title = "Rain on Leaves",
            artist = "Slow Sleep",
            imageUrl = "https://images.unsplash.com/photo-1502082553048-f009c37129b9?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3"
        ),
        Track(
            title = "Soft Piano",
            artist = "LoFi Dreams",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        ),
        Track(
            title = "Night Forest",
            artist = "Zen Garden",
            imageUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
        ),
        Track(
            title = "Deep Focus",
            artist = "Aurora",
            imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
        ),
        Track(
            title = "Starry Night",
            artist = "Nocturne",
            imageUrl = "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
        ),
        Track(
            title = "Calm Breeze",
            artist = "Midnight Air",
            imageUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"
        ),
        Track(
            title = "Sunset Glow",
            artist = "Serene",
            imageUrl = "https://images.unsplash.com/photo-1501973801540-537f08ccae7b?w=800",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calm_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvTracks)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = CalmTrackAdapter(tracks) { track ->
            val intent = Intent(requireContext(), MusicPlayerActivity::class.java).apply {
                putExtra(MusicPlayerActivity.EXTRA_TITLE, track.title)
                putExtra(MusicPlayerActivity.EXTRA_ARTIST, track.artist)
                putExtra(MusicPlayerActivity.EXTRA_IMAGE, track.imageUrl)
                putExtra(MusicPlayerActivity.EXTRA_AUDIO, track.audioUrl)
            }
            startActivity(intent)
        }
    }
}
