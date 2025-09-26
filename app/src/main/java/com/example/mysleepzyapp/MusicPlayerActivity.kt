package com.example.mysleepzyapp

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_AUDIO = "extra_audio"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        val back = findViewById<ImageView>(R.id.btnBack)
        val cover = findViewById<ImageView>(R.id.imgCoverLarge)
        val title = findViewById<TextView>(R.id.txtTitle)
        val artist = findViewById<TextView>(R.id.txtArtist)
        val playPause = findViewById<ImageButton>(R.id.btnPlayPause)
        val seek = findViewById<SeekBar>(R.id.seekbar)

        val trackTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown"
        val trackArtist = intent.getStringExtra(EXTRA_ARTIST) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE)
        val audioUrl = intent.getStringExtra(EXTRA_AUDIO)

        title.text = trackTitle
        artist.text = trackArtist
        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_profile).into(cover)

        back.setOnClickListener { finish() }

        if (!audioUrl.isNullOrBlank()) {
            preparePlayer(audioUrl) {
                // Ready
                seek.max = mediaPlayer?.duration ?: 0
                mediaPlayer?.start()
                playPause.setImageResource(R.drawable.ic_pause)
                startProgressUpdates(seek)
            }
        }

        playPause.setOnClickListener {
            val mp = mediaPlayer ?: return@setOnClickListener
            if (mp.isPlaying) {
                mp.pause()
                playPause.setImageResource(R.drawable.ic_play)
            } else {
                mp.start()
                playPause.setImageResource(R.drawable.ic_pause)
            }
        }

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun preparePlayer(url: String, onReady: () -> Unit) {
        releasePlayer()
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        mp.setDataSource(this, Uri.parse(url))
        mp.setOnPreparedListener {
            onReady()
        }
        mp.setOnCompletionListener {
            // reset play icon at end
            findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.ic_play)
        }
        mp.prepareAsync()
        mediaPlayer = mp
    }

    private fun startProgressUpdates(seek: SeekBar) {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (isActive) {
                mediaPlayer?.let { m ->
                    if (m.isPlaying) {
                        seek.progress = m.currentPosition
                    }
                }
                delay(500)
            }
        }
    }

    private fun releasePlayer() {
        progressJob?.cancel()
        progressJob = null
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        // Pause playback when leaving screen
        mediaPlayer?.pause()
        findViewById<ImageButton>(R.id.btnPlayPause)?.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
