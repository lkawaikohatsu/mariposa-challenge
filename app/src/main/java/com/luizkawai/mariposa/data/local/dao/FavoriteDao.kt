package com.luizkawai.mariposa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.luizkawai.mariposa.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_characters ORDER BY name")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT character_id FROM favorite_characters")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_characters WHERE character_id = :characterId)")
    suspend fun isFavorite(characterId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_characters WHERE character_id = :characterId")
    suspend fun deleteById(characterId: Int)

    @Transaction
    suspend fun toggle(favorite: FavoriteEntity) {
        if (isFavorite(favorite.id)) {
            deleteById(favorite.id)
        } else {
            insert(favorite)
        }
    }
}
