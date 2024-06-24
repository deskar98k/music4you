package ru.samsung.kotlin.music4you.ui

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.samsung.kotlin.music4you.data.AppDatabase
import ru.samsung.kotlin.music4you.data.Music
import ru.samsung.kotlin.music4you.databinding.FragmentMusicBinding
import ru.samsung.kotlin.music4you.databinding.MusicItemBinding

class MusicFragment : Fragment() {

    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001
    private lateinit var viewModel: MusicViewModel // Объявление ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MusicViewModel::class.java) // Инициализация ViewModel

        binding.swipeRefresh.setOnRefreshListener {
            Log.d("MusicFragment", "SwipeRefresh triggered")
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
                )
            } else {
                loadMusicFromExternalStorage()
            }
        }

        // Запуск загрузки музыки при первом запуске
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadMusicFromExternalStorage()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        }

        observeSelectedMusic()
    }

    private fun observeSelectedMusic() {
        viewModel.musicList.observe(viewLifecycleOwner, { musicList ->
            updateMusicLayout(musicList)
        })

        viewModel.selectedMusic.observe(viewLifecycleOwner, { music ->
            // Handle selected music item
            Log.d("MusicFragment", "Selected music: $music")
            // Update UI or perform any action based on selected music
        })
    }

    fun searchMusic(query: String) {
        viewModel.searchMusic(query)
    }

    private fun loadMusicFromDatabase(query: String? = null) {
        lifecycleScope.launch {
            try {
                val musicDao = AppDatabase.getDatabase(requireContext()).musicDao()
                val musicList = if (query.isNullOrEmpty()) {
                    musicDao.getAllMusic()
                } else {
                    musicDao.searchMusic("%$query%")
                }
                Log.d("MusicFragment", "Loaded ${musicList.size} music items from database")
                updateMusicLayout(musicList)
            } catch (e: Exception) {
                Log.e("MusicFragment", "Error loading music from database", e)
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadMusicFromExternalStorage() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch(Dispatchers.IO) {
            val musicList = searchMusicFiles()
            saveMusicToDatabase(musicList)
            withContext(Dispatchers.Main) {
                loadMusicFromDatabase()
            }
        }
    }

    private fun searchMusicFiles(): List<Music> {
        val musicList = mutableListOf<Music>()
        val contentResolver = requireContext().contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, null, null)

        cursor?.use {
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val title = it.getString(titleIndex) ?: ""
                val artist = it.getString(artistIndex) ?: ""
                val data = it.getString(dataIndex) ?: ""
                val duration = it.getInt(durationIndex)

                musicList.add(
                    Music(
                        title = title,
                        author = artist,
                        audioFilePath = data,
                        duration = duration,
                        albumArtPath = data // Сохраняем путь к аудиофайлу
                    )
                )
            }
        }
        return musicList
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
            Log.e("MusicFragment", "Failed to load album art from file: $filePath", e)
            null
        } finally {
            retriever.release()
        }
    }

    private suspend fun saveMusicToDatabase(musicList: List<Music>) {
        val musicDao = AppDatabase.getDatabase(requireContext()).musicDao()
        val existingMusic = musicDao.getAllMusic()
        val newMusic =
            musicList.filter { music -> existingMusic.none { it.audioFilePath == music.audioFilePath } }
        if (newMusic.isNotEmpty()) {
            musicDao.insertMusic(newMusic)
            Log.d("MusicFragment", "Inserted ${newMusic.size} new music items into database")
        }
    }

    private fun updateMusicLayout(musicList: List<Music>) {
        binding.musicLayout.removeAllViews()
        for (music in musicList) {
            val itemBinding =
                MusicItemBinding.inflate(layoutInflater, binding.musicLayout, false)
            itemBinding.musicName.text = music.title
            itemBinding.musicAutor.text = music.author
            itemBinding.musicDuration.text = formatDuration(music.duration)

            // Устанавливаем обложку альбома, если она есть
            val albumArt = getAlbumArtFromFilePath(music.audioFilePath)
            albumArt?.let {
                itemBinding.musicIcon.setImageBitmap(it)
            }

            itemBinding.root.setOnClickListener {
                viewModel.selectMusic(music) // Выбираем музыку через ViewModel
            }

            binding.musicLayout.addView(itemBinding.root)
        }
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 60000
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusicFromExternalStorage()
        } else {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
