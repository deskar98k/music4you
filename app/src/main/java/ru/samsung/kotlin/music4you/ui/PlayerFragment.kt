package ru.samsung.kotlin.music4you.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.samsung.kotlin.music4you.R
import ru.samsung.kotlin.music4you.data.Music
import ru.samsung.kotlin.music4you.databinding.FragmentPlayerBinding

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var viewModel: MusicViewModel

    private var isRepeatEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer()
        viewModel = ViewModelProvider(requireActivity()).get(MusicViewModel::class.java)

        binding.apply {
            songProgressSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
            playerPlayPauseButton.setOnClickListener { togglePlayPause() }
            playerPreviousButton.setOnClickListener { playPreviousSong() }
            playerNextButton.setOnClickListener { playNextSong() }
            playerRepeatButton.setOnClickListener { toggleRepeat() }
        }

        observeSelectedMusic()
    }

    private fun observeSelectedMusic() {
        viewModel.selectedMusic.observe(viewLifecycleOwner) { music ->
            music?.let {
                selectSongAndPlay(it)
            }
        }
    }

    private fun selectSongAndPlay(music: Music) {
        mediaPlayer.apply {
            reset()
            setDataSource(music.audioFilePath)
            prepareAsync()
            setOnPreparedListener {
                start()
                updateSongInfo(music)
                updateSeekBar()
            }
            setOnCompletionListener {
                if (isRepeatEnabled) {
                    start()
                } else {
                    playNextSong()
                }
            }
        }
    }

    private fun playNextSong() {
        val currentIndex = viewModel.getCurrentIndex()
        val nextIndex = (currentIndex + 1) % viewModel.getCurrentMusicListSize()
        viewModel.selectMusic(viewModel.getCurrentMusicList()!![nextIndex])
    }

    private fun playPreviousSong() {
        val currentIndex = viewModel.getCurrentIndex()
        val prevIndex = if (currentIndex - 1 < 0) {
            viewModel.getCurrentMusicListSize() - 1
        } else {
            currentIndex - 1
        }
        viewModel.selectMusic(viewModel.getCurrentMusicList()!![prevIndex])
    }

    private fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
        binding.playerRepeatButton.isSelected = isRepeatEnabled
        binding.playerRepeatButton.setImageResource(
            if (isRepeatEnabled) {
                R.drawable.ic_repeat_on
            } else {
                R.drawable.ic_repeat_off
            }
        )
    }

    private fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
            updateSeekBar()
        }
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        binding.playerPlayPauseButton.setImageResource(
            if (mediaPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                mediaPlayer.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            // Not needed
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // Not needed
        }
    }

    private fun updateSeekBar() {
        binding.songProgressSeekBar.max = mediaPlayer.duration
        lifecycleScope.launch {
            while (mediaPlayer.isPlaying || mediaPlayer.currentPosition < mediaPlayer.duration) {
                binding.songProgressSeekBar.progress = mediaPlayer.currentPosition
                binding.songDuration.text = formatDuration(mediaPlayer.currentPosition)
                delay(1000) // Update seekbar every second
            }
        }
    }

    private fun updateSongInfo(music: Music) {
        binding.apply {
            songTitle.text = music.title
            songArtist.text = music.author
            songDuration.text = formatDuration(mediaPlayer.duration)
            // Set album art if available
            val albumArt = getAlbumArtFromFilePath(music.audioFilePath)
            albumArt?.let {
                songImage.setImageBitmap(it)
            }
        }
    }

    private fun getAlbumArtFromFilePath(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            if (art != null) {
                BitmapFactory.decodeByteArray(art, 0, art.size)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PlayerFragment", "Failed to load album art from file: $filePath", e)
            null
        } finally {
            retriever.release()
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 60000
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer.release()
    }
}
