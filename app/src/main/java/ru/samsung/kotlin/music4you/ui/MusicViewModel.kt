package ru.samsung.kotlin.music4you.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.samsung.kotlin.music4you.data.AppDatabase
import ru.samsung.kotlin.music4you.data.Music

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicDao = AppDatabase.getDatabase(application).musicDao()
    private val _musicList = MutableLiveData<List<Music>>()
    val musicList: LiveData<List<Music>>
        get() = _musicList

    private val _selectedMusic = MutableLiveData<Music>()
    val selectedMusic: LiveData<Music>
        get() = _selectedMusic

    // Добавленные переменные для управления текущим выбранным треком и списком музыки
    private var currentIndex: Int = -1

    // Функция для получения размера текущего списка музыки
    fun getCurrentMusicListSize(): Int {
        return _musicList.value?.size ?: 0
    }

    // Функция для получения текущего списка музыки
    fun getCurrentMusicList(): List<Music>? {
        return _musicList.value
    }

    init {
        loadAllMusic()
    }

    private fun loadAllMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            _musicList.postValue(musicDao.getAllMusic())
        }
    }

    fun searchMusic(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _musicList.postValue(musicDao.searchMusic("%$query%"))
        }
    }

    fun selectMusic(music: Music) {
        _selectedMusic.value = music
        currentIndex = _musicList.value?.indexOf(music) ?: -1
    }

    // Добавленные функции для управления текущим выбранным треком
    fun getCurrentIndex(): Int {
        return currentIndex
    }

    fun getNextMusic(): Music? {
        currentIndex++
        return if (currentIndex < (_musicList.value?.size ?: 0)) {
            _musicList.value?.get(currentIndex)
        } else {
            null
        }
    }

    fun getPreviousMusic(): Music? {
        currentIndex--
        return if (currentIndex >= 0 && currentIndex < (_musicList.value?.size ?: 0)) {
            _musicList.value?.get(currentIndex)
        } else {
            null
        }
    }
}
