package ru.samsung.kotlin.music4you.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music")
data class Music(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val audioFilePath: String,
    val duration: Int,
    val albumArtPath: String? // Путь к аудиофайлу
)
