package com.luizkawai.mariposa.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.luizkawai.mariposa.data.local.dao.FavoriteDao
import com.luizkawai.mariposa.data.local.entity.FavoriteEntity

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class)
abstract class CharacterDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
