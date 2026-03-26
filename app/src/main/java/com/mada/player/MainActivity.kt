package com.mada.player

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mada.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        playMedia(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPlayer()
        setupUi()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.setShowNextButton(false)
        binding.playerView.setShowPreviousButton(false)

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                binding.statusText.text = if (isPlaying) "Playing" else "Paused"
            }
        })
    }

    private fun setupUi() {
        binding.openButton.setOnClickListener {
            openDocument.launch(arrayOf("audio/*", "video/*"))
        }

        binding.playButton.setOnClickListener {
            if (::player.isInitialized) {
                player.play()
            }
        }

        binding.pauseButton.setOnClickListener {
            if (::player.isInitialized) {
                player.pause()
            }
        }

        binding.stopButton.setOnClickListener {
            if (::player.isInitialized) {
                player.pause()
                player.seekTo(0)
                binding.statusText.text = "Stopped"
            }
        }
    }

    private fun playMedia(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        binding.fileNameText.text = uri.lastPathSegment ?: "Selected media"
        binding.statusText.text = "Loading..."
    }

    override fun onStart() {
        super.onStart()
        binding.playerView.onResume()
    }

    override fun onStop() {
        binding.playerView.onPause()
        super.onStop()
    }

    override fun onDestroy() {
        if (::player.isInitialized) {
            player.release()
        }
        super.onDestroy()
    }
}
