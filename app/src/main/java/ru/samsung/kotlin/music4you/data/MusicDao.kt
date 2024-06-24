package ru.samsung.kotlin.music4you.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MusicDao {
    @Query("SELECT * FROM music")
    suspend fun getAllMusic(): List<Music>

    @Insert
    suspend fun insertMusic(music: List<Music>)

    @Query("SELECT * FROM music WHERE title LIKE :query OR author LIKE :query")
    suspend fun searchMusic(query: String): List<Music>
}
